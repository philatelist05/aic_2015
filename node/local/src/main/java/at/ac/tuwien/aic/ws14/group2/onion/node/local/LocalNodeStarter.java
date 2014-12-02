package at.ac.tuwien.aic.ws14.group2.onion.node.local;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.DirectoryService;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.RSAKeyGenerator;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorkerFactory;
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
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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

		logger.info("Generating RSA keypair"); //TODO is this needed somewhere?
		KeyPair rsaKeyPair = null;

		try {
			RSAKeyGenerator keyGenerator = new RSAKeyGenerator();
			rsaKeyPair = keyGenerator.generateKeys(0);
		} catch (NoSuchProviderException e) {
			logger.fatal("Provider not available, exiting..");
			logger.debug(e.getMessage());
			System.exit(-1);
		} catch (NoSuchAlgorithmException e) {
			logger.fatal("RSA algorithm not available, exiting..");
			logger.debug(e.getMessage());
			System.exit(-1);
		}

		logger.info("Creating NodeCore");
		LocalNodeCore nodeCore = new LocalNodeCore();

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

		logger.debug("Creating SSL Transport using Thrift");
		TTransport transport = null;
		try {
			transport = TSSLTransportFactory.getClientSocket("localhost", configuration.getNodeCommonPort(), 0, clientParams);
		} catch (TTransportException e) {
			logger.fatal("Could not establish SSL connection to directory, exiting..");
			logger.catching(Level.DEBUG, e);
			System.exit(-1);
		}

		logger.debug("Creating Thrift client");
		TProtocol protocol = new TBinaryProtocol(transport);
		DirectoryService.Client client = new DirectoryService.Client(protocol);

		// Create and start SOCKS server
		logger.debug("Creating and starting SOCKS server");
		SocksServer socksServer = new SocksServer(configuration.getLocalNodeServerPort(), nodeCore, client);
		socksServer.setUncaughtExceptionHandler((thread, throwable) -> logger.error("Uncaught exception in thread: " + thread.getName(), throwable));
		socksServer.start();

		// Block main thread until SOCKS server is interrupted
		logger.debug("Waiting for SOCKS server to be interrupted");
		try {
			socksServer.join();
		} catch (InterruptedException ignored) {
		}
	}
}
