package at.ac.tuwien.aic.ws14.group2.onion.directory.api;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.DirectoryService;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Thomas on 26.10.2014.
 */
public class ThriftTest {

    private static final int THRIFT_PORT = 9090;
    private static File keyStoreFile;

    private static class ServiceImpl implements DirectoryService.Iface {

        public volatile boolean called;

        @Override
        public void ping() throws TException {
            called = true;
        }

        @Override
        public boolean heartbeat(ChainNodeInformation nodeInformation, NodeUsage nodeUsage) throws TException {
            return false;
        }

        @Override
        public boolean registerNode(ChainNodeInformation nodeInformation) throws TException {
            return false;
        }

        @Override
        public List<ChainNodeInformation> getChain(int chainLength) throws TException {
            return null;
        }
    }

    @Before
    public void setUp() {
        ClassLoader cl = ThriftTest.class.getClassLoader();
        keyStoreFile = null;
        try {
            InputStream input = cl.getResourceAsStream("keys/thrift-test.jks");
            keyStoreFile = File.createTempFile("thrifttest-ks", ".tmp");
            OutputStream out = new FileOutputStream(keyStoreFile);
            int read;
            byte[] bytes = new byte[1024];

            while ((read = input.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            keyStoreFile.deleteOnExit();
        } catch (IOException e) {
        }
    }
    
    @Test
    public void cleartext() throws TException, InterruptedException {

        // Server Code

        ServiceImpl handler = new ServiceImpl();
        DirectoryService.Processor<DirectoryService.Iface> processor = new DirectoryService.Processor<>(handler);

        TServerTransport serverTransport = new TServerSocket(9090);
        final TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));

        Runnable serverMethod = () -> {
            System.out.println("server started");
            server.serve();
            System.out.println("server finished");
        };

        Thread serverThread = new Thread(serverMethod);
        serverThread.start();


        // Client Code

        TTransport transport = new TSocket("localhost", THRIFT_PORT);
        transport.open();

        TProtocol protocol = new TBinaryProtocol(transport);
        DirectoryService.Client client = new DirectoryService.Client(protocol);

        assertEquals(false, handler.called);
        client.ping();
        assertEquals(true, handler.called);

        transport.close();

        server.stop();
        serverThread.join();
    }

    @Test
    public void secure() throws TException, InterruptedException {

        // Server Code

        ServiceImpl handler = new ServiceImpl();
        DirectoryService.Processor<DirectoryService.Iface> processor = new DirectoryService.Processor<>(handler);

        TSSLTransportFactory.TSSLTransportParameters serverParams = new TSSLTransportFactory.TSSLTransportParameters();
        serverParams.setKeyStore(keyStoreFile.getPath(), "password");

        TServerTransport serverTransport = TSSLTransportFactory.getServerSocket(9090, 0, null, serverParams);
        final TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));

        Runnable serverMethod = () -> {
            System.out.println("server started");
            server.serve();
            System.out.println("server finished");
        };

        Thread serverThread = new Thread(serverMethod);
        serverThread.start();


        // Client Code

        TSSLTransportFactory.TSSLTransportParameters clientParams = new TSSLTransportFactory.TSSLTransportParameters();
        clientParams.setTrustStore(keyStoreFile.getPath(), "password");

        TTransport transport = TSSLTransportFactory.getClientSocket("localhost", THRIFT_PORT, 0, clientParams);

        TProtocol protocol = new TBinaryProtocol(transport);
        DirectoryService.Client client = new DirectoryService.Client(protocol);

        assertEquals(false, handler.called);
        client.ping();
        assertEquals(true, handler.called);

        transport.close();

        server.stop();
        serverThread.join();
    }
}
