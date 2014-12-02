package at.ac.tuwien.aic.ws14.group2.onion.node.chain;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.DirectoryService;
import at.ac.tuwien.aic.ws14.group2.onion.node.chain.heartbeat.HeartBeatWorker;
import at.ac.tuwien.aic.ws14.group2.onion.node.chain.node.ChainCellWorkerFactory;
import at.ac.tuwien.aic.ws14.group2.onion.node.chain.node.ChainNodeCore;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.RSAKeyGenerator;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorkerFactory;
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
import org.bouncycastle.util.encoders.Base64;

import java.io.*;
import java.net.ServerSocket;
import java.net.URL;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

public class ChainNodeStarter {
    private static final Logger logger = LogManager.getLogger(ChainNodeStarter.class.getName());

    public static void main(String[] args) {
        logger.info("Reading configuration parameters");
        Configuration configuration = ConfigurationFactory.getConfiguration();

        logger.info("Adding BouncyCastle provider");
        Security.addProvider(new BouncyCastleProvider());

        logger.info("Generating RSA keypair");
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

        logger.info("Setting up CellWorkerFactory");
        ConnectionWorkerFactory.setCellWorkerFactory(new ChainCellWorkerFactory(rsaKeyPair.getPrivate()));

        logger.info("Starting node core");
        ServerSocket listeningSocket = null;
        try {
            listeningSocket = new ServerSocket(0, 100);
        } catch (IOException e) {
            logger.fatal("Failed to create listening Socket!");
            System.exit(-1);
        }
        Thread nodeCoreThread = new Thread(new ChainNodeCore(listeningSocket));
        nodeCoreThread.start();

        String directoryHostname = "localhost";
        String chainNodeHostname = "localhost";
        if (!configuration.isLocalMode()) {
            directoryHostname = configuration.getNodeCommonHost();
            URL awsCheckIp;
            try {
                awsCheckIp = new URL("http://checkip.amazonaws.com/");
                BufferedReader in = new BufferedReader(new InputStreamReader(awsCheckIp.openStream()));
                chainNodeHostname = in.readLine();
                in.close();
            } catch (Exception e) {
                logger.fatal("Could not determine public IP, aborting.");
                logger.catching(Level.DEBUG, e);
                System.exit(-1);
            }
        }
        ChainNodeInformation nodeInformation = new ChainNodeInformation(listeningSocket.getLocalPort(), chainNodeHostname, Base64.toBase64String(rsaKeyPair.getPublic().getEncoded()));
        logger.info("ChainNodeInformation: {}", nodeInformation);


        logger.info("Establishing Thrift client connection");
        long sleepInterval = configuration.getChainNodeHeartbeatInterval();

        logger.info("Creating temp file for keystore");
        ClassLoader cl = ChainNodeStarter.class.getClassLoader();
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
            transport = TSSLTransportFactory.getClientSocket(directoryHostname, configuration.getNodeCommonPort(), 0, clientParams);
        } catch (TTransportException e) {
            logger.fatal("Could not establish SSL connection to directory, exiting..");
            logger.catching(Level.DEBUG, e);
            System.exit(-1);
        }

        logger.debug("Creating Thrift client");
        TProtocol protocol = new TBinaryProtocol(transport);
        DirectoryService.Client client = new DirectoryService.Client(protocol);

        logger.info("Starting Heartbeat worker thread");
        Thread heartBeatWorker = new Thread(new HeartBeatWorker(client, nodeInformation, sleepInterval, rsaKeyPair.getPrivate()));
        heartBeatWorker.start();
    }
}
