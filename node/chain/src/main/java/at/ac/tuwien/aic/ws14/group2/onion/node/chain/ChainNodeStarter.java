package at.ac.tuwien.aic.ws14.group2.onion.node.chain;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.DirectoryService;
import at.ac.tuwien.aic.ws14.group2.onion.node.chain.heartbeat.HeartBeatWorker;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.RSAKeyGenerator;
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

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

public class ChainNodeStarter {
    static final Logger logger = LogManager.getLogger(ChainNodeStarter.class.getName());

    private static final int THRIFT_PORT = 9091;    // TODO read from config
    private static final int NODE_PORT = (int) (9092 + (100 * Math.random()));

    public static void main(String[] args) {

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

        ChainNodeInformation nodeInformation = new ChainNodeInformation(NODE_PORT, "localhost", Base64.toBase64String(rsaKeyPair.getPublic().getEncoded()));
        logger.info("ChainNodeInformation: {}", nodeInformation);

        logger.info("Establishing Thrift client connection");
        long sleepInterval = 2000; //TODO read from config

        TSSLTransportFactory.TSSLTransportParameters clientParams = new TSSLTransportFactory.TSSLTransportParameters();
        clientParams.setTrustStore("keys/thrift-directory-clients.jks", "password");  //TODO use keystore with directory public key here!

        logger.debug("Creating SSL Transport using Thrift");
        TTransport transport = null;
        try {
            transport = TSSLTransportFactory.getClientSocket("localhost", THRIFT_PORT, 0, clientParams);
        } catch (TTransportException e) {
            logger.fatal("Could not establish SSL connection to directory, exiting..");
            logger.catching(Level.DEBUG, e);
            System.exit(-1);
        }

        logger.debug("Creating Thrift client");
        TProtocol protocol = new TBinaryProtocol(transport);
        DirectoryService.Client client = new DirectoryService.Client(protocol);

        logger.debug("Starting Heartbeat worker thread");
        Thread heartBeatWorker = new Thread(new HeartBeatWorker(client, nodeInformation, sleepInterval, rsaKeyPair.getPrivate()));
        heartBeatWorker.setDaemon(true);
        heartBeatWorker.run();


    }
}
