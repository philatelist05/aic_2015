package at.ac.tuwien.aic.ws14.group2.onion.directory;

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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DirectoryStarter {

    static final Logger logger = LogManager.getLogger(DirectoryStarter.class.getName());

    private static final int THRIFT_PORT = 9091;    // TODO read from config

    public static void main(String[] args) {
        logger.info("Starting Directory server..");

        ChainNodeRegistry chainNodeRegistry = new ChainNodeRegistry();

        ServiceImplementation handler = new ServiceImplementation(chainNodeRegistry);
        DirectoryService.Processor<DirectoryService.Iface> processor = new DirectoryService.Processor<>(handler);

        TSSLTransportFactory.TSSLTransportParameters serverParams = new TSSLTransportFactory.TSSLTransportParameters();
        serverParams.setKeyStore("keys/thrift-directory.jks", "password");

        TServerTransport serverTransport = null;
        try {
            serverTransport = TSSLTransportFactory.getServerSocket(THRIFT_PORT, 0, null, serverParams);
        } catch (TTransportException e) {
            logger.fatal("Could not establish Thrift Service, shutting down..");
            logger.catching(Level.DEBUG, e);
            System.exit(-1);
        }
        TThreadPoolServer.Args serverArgs = new TThreadPoolServer.Args(serverTransport).processor(processor);
        serverArgs.minWorkerThreads(5);
        serverArgs.minWorkerThreads(15);
        final TServer server = new TThreadPoolServer(serverArgs);

        Runnable serverMethod = () -> {
            logger.debug("server started");
            server.serve();
            logger.debug("server finished");
        };

        Thread serverThread = new Thread(serverMethod);
        serverThread.start();

        ScheduledExecutorService scheduledThreadPoolExecutor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("chainNodeMonitor"));
        scheduledThreadPoolExecutor.scheduleAtFixedRate(new ChainNodeMonitor(chainNodeRegistry, 2000), 0, 5000, TimeUnit.MILLISECONDS);

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            logger.info("Interrupted, exiting..");
            System.exit(0);
        }
    }
}
