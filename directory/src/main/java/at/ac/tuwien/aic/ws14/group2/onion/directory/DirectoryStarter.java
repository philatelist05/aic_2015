package at.ac.tuwien.aic.ws14.group2.onion.directory;

import at.ac.tuwien.aic.ws14.group2.onion.common.Configuration;
import at.ac.tuwien.aic.ws14.group2.onion.common.ConfigurationFactory;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.DirectoryService;
import at.ac.tuwien.aic.ws14.group2.onion.directory.handler.ServiceImplementation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.worker.ChainNodeMonitor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.async.DaemonThreadFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DirectoryStarter {

    static final Logger logger = LogManager.getLogger(DirectoryStarter.class.getName());

    public static void main(String[] args) {
        logger.info("Reading configuration");
        Configuration configuration = ConfigurationFactory.getConfiguration();

        logger.info("Starting Directory server..");

        ChainNodeRegistry chainNodeRegistry = new ChainNodeRegistry();

        ServiceImplementation handler = new ServiceImplementation(chainNodeRegistry);
        DirectoryService.Processor<DirectoryService.Iface> processor = new DirectoryService.Processor<>(handler);

        logger.info("Creating temp file for keystore");
        ClassLoader cl = DirectoryStarter.class.getClassLoader();
        File keyStoreFile = null;
        try {
            InputStream input = cl.getResourceAsStream("keys/thrift-directory.jks");
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
        TSSLTransportFactory.TSSLTransportParameters serverParams = new TSSLTransportFactory.TSSLTransportParameters();
        serverParams.setKeyStore(keyStoreFile.getPath(), "password");

        TServerTransport serverTransport = null;
        try {
            serverTransport = TSSLTransportFactory.getServerSocket(configuration.getNodeCommonPort(), 0, null, serverParams);
        } catch (TTransportException e) {
            logger.fatal("Could not establish Thrift Service, shutting down..");
            logger.catching(Level.DEBUG, e);
            System.exit(-1);
        }
        TThreadPoolServer.Args serverArgs = new TThreadPoolServer.Args(serverTransport).processor(processor);
        serverArgs.minWorkerThreads(configuration.getDirectoryNodeMinThriftWorker());
        serverArgs.minWorkerThreads(configuration.getDirectoryNodeMaxThriftWorker());
        final TServer server = new TThreadPoolServer(serverArgs);

        Runnable serverMethod = () -> {
            logger.debug("server started");
            server.serve();
            logger.debug("server finished");
        };

        Thread serverThread = new Thread(serverMethod);
        serverThread.start();

        ScheduledExecutorService scheduledThreadPoolExecutor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("chainNodeMonitor"));
        scheduledThreadPoolExecutor.scheduleAtFixedRate(
                new ChainNodeMonitor(chainNodeRegistry, configuration.getChainNodeHeartbeatInterval()),
                0, configuration.getDirectoryNodeHeartbeatTimeout(), TimeUnit.MILLISECONDS);

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            logger.info("Interrupted, exiting..");
            System.exit(0);
        }
    }
}
