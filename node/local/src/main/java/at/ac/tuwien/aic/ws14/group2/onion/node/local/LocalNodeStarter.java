package at.ac.tuwien.aic.ws14.group2.onion.node.local;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.DirectoryService;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorkerFactory;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.LocalCellWorkerFactory;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.LocalNodeCore;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.server.SocksServer;
import at.ac.tuwien.aic.ws14.group2.onion.shared.Configuration;
import at.ac.tuwien.aic.ws14.group2.onion.shared.ConfigurationFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.net.ServerSocket;
import java.net.URL;
import java.security.Security;

public class LocalNodeStarter {
	private static final Logger logger = LogManager.getLogger(LocalNodeStarter.class.getName());

	/**
	 * Main method
	 *
	 * @param args CLI arguments
	 */
	public static void main(String[] args) throws IOException {
		logger.info("Reading configuration");
		Configuration configuration = ConfigurationFactory.getConfiguration();

		logger.info("Adding BouncyCastle provider");
		Security.addProvider(new BouncyCastleProvider());

		String directoryHost = "localhost";
		String localHost = "localhost";

		if (!configuration.isLocalMode()) {
			directoryHost = configuration.getNodeCommonHost();
			URL awsCheckIp;
			try {
				awsCheckIp = new URL("http://checkip.amazonaws.com/");
				BufferedReader in = new BufferedReader(new InputStreamReader(awsCheckIp.openStream()));
				localHost = in.readLine();
				in.close();
			} catch (Exception e) {
				logger.fatal("Could not determine public IP, aborting.");
				logger.catching(Level.DEBUG, e);
				System.exit(-1);
			}
		}

		ServerSocket fakeListeningSocket = null;
		int listeningPort = 30000;
		while (fakeListeningSocket == null && listeningPort < 39999) {
			listeningPort++;
			try {
				fakeListeningSocket = new ServerSocket(listeningPort, 100);
			} catch (IOException e) {
			}
		}

		if (fakeListeningSocket == null) {
			logger.fatal("Failed to create listening Socket!");
			System.exit(-1);
		}

		logger.info("Creating NodeCore");
		Endpoint fakeEndpoint = new Endpoint(localHost, listeningPort);
		LocalNodeCore nodeCore = new LocalNodeCore(fakeEndpoint);

		logger.info("Setting up CellWorkerFactory");
		ConnectionWorkerFactory.setCellWorkerFactory(new LocalCellWorkerFactory(nodeCore));

		logger.info("Establishing Thrift client connection");
		logger.info("Creating temp file for keystore");
		ClassLoader cl = LocalNodeStarter.class.getClassLoader();
		File keyStoreFile = null;
		try {
			InputStream input = cl.getResourceAsStream("keys/thrift-directory-clients.jks");
			keyStoreFile = File.createTempFile("directory-ks", ".tmp");
			OutputStream out = new FileOutputStream(keyStoreFile);
			int read;
			byte[] bytes = new byte[1024];

			while ((read = input.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			keyStoreFile.deleteOnExit();
		} catch (IOException e) {
			logger.fatal("Could not load keys for Thrift service");
			logger.catching(Level.DEBUG, e);
			System.exit(-1);
		}

		TSSLTransportFactory.TSSLTransportParameters clientParams = new TSSLTransportFactory.TSSLTransportParameters();
		clientParams.setTrustStore(keyStoreFile.getPath(), "password");  //TODO use keystore with directory public key here!

		logger.info("Creating SSL Transport using Thrift");
		TTransport transport = null;

		try {
			transport = TSSLTransportFactory.getClientSocket(directoryHost, configuration.getNodeCommonPort(), 0, clientParams);
		} catch (TTransportException e) {
			logger.fatal("Could not establish SSL connection to directory, exiting..");
			logger.catching(Level.DEBUG, e);
			System.exit(-1);
		}

		logger.info("Creating Thrift client");
		TProtocol protocol = new TBinaryProtocol(transport);
		DirectoryService.Client client = new DirectoryService.Client(protocol);

		// Create and start SOCKS server
		logger.info("Creating and starting SOCKS server");
		SocksServer socksServer = new SocksServer(configuration.getLocalNodeServerPort(), nodeCore, client);
		socksServer.setUncaughtExceptionHandler((thread, throwable) -> logger.error("Uncaught exception in thread: " + thread.getName(), throwable));
		socksServer.start();

		// Block main thread until SOCKS server is interrupted
		logger.info("Waiting for SOCKS server to be interrupted");
		try {
			socksServer.join();
		} catch (InterruptedException ignored) {
		}
	}
}
