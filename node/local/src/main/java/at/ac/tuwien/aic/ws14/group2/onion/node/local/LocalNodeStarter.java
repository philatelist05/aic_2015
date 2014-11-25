package at.ac.tuwien.aic.ws14.group2.onion.node.local;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.DirectoryService;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.RSAKeyGenerator;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorkerFactory;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.LocalCellWorkerFactory;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.LocalNodeCore;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

public class LocalNodeStarter {
    private static final Logger logger = LogManager.getLogger(LocalNodeStarter.class.getName());
    private static final int THRIFT_PORT = 9091; // TODO read from config

    /**
     * Main method
     * @param args CLI arguments
     */
    public static void main(String [] args) {
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

        //TODO Create and start SOCKS server here..
    }
}
