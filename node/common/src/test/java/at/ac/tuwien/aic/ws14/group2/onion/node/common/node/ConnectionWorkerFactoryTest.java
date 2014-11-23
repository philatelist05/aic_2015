package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.NodeIDExistsAlreadyException;
import org.junit.Test;
import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by Thomas on 22.11.2014.
 */
public class ConnectionWorkerFactoryTest {

    private static final int SERVER_PORT_1 = 10100;
    private static final int SERVER_PORT_2 = 10101;

    @Test
    public void getConnectionWorker() throws IOException {
        try (
            ServerSocket server1 = new ServerSocket(SERVER_PORT_1);
            ServerSocket server2 = new ServerSocket(SERVER_PORT_2);
        ) {
            Endpoint e1 = new Endpoint(InetAddress.getByName("localhost"), SERVER_PORT_1);
            Endpoint e2 = new Endpoint(InetAddress.getByName("localhost"), SERVER_PORT_2);
            Endpoint e3 = new Endpoint(InetAddress.getByName("localhost"), SERVER_PORT_1);

            CellWorkerFactory cellWorkerFactory = mock(CellWorkerFactory.class);

            ConnectionWorkerFactory conWorkerFactory = new ConnectionWorkerFactory(cellWorkerFactory);

            try (
                ConnectionWorker c1 = conWorkerFactory.getConnectionWorker(e1);
                ConnectionWorker c2 = conWorkerFactory.getConnectionWorker(e2);
                ConnectionWorker c3 = conWorkerFactory.getConnectionWorker(e3);
            ) {
                assertSame(c1, c3);
                assertNotSame(c1, c2);
                assertNotSame(c2, c3);

                c1.close();
                c2.close();
                c3.close();
            }
        }
    }

    @Test
    public void createIncomingConnectionWorker() throws Exception {
        try (ServerSocket server1 = new ServerSocket(SERVER_PORT_1)) {
            Socket client1 = new Socket("localhost", SERVER_PORT_1);
            Socket client2 = new Socket("localhost", SERVER_PORT_1);

            Socket acceptedSocket1 = server1.accept();
            Socket acceptedSocket2 = server1.accept();

            CellWorkerFactory cellWorkerFactory = mock(CellWorkerFactory.class);

            ConnectionWorkerFactory conWorkerFactory = new ConnectionWorkerFactory(cellWorkerFactory);
            ConnectionWorker c1 = conWorkerFactory.createIncomingConnectionWorker(acceptedSocket1);
            ConnectionWorker c2 = conWorkerFactory.createIncomingConnectionWorker(acceptedSocket2);

            assertNotNull(c1);
            assertNotNull(c2);

            assertNotSame(c1, c2);
        }
    }
}
