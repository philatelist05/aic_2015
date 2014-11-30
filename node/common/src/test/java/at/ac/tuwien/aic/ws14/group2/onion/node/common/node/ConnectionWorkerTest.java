package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.Cell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.CreateCell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.DHHalf;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.EncryptedDHHalf;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.DHKeyExchange;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.RSAKeyGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Vector;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Thomas on 23.11.2014.
 */
public class ConnectionWorkerTest {

    private static final int SERVER_PORT_1 = 10100;

    private static final BigInteger g = DHKeyExchange.generateRelativePrime();
    private static final BigInteger p = DHKeyExchange.generateRelativePrime();
    private static KeyPair keyPair;


    private EncryptedDHHalf createEncryptedDHHalf() {
        DHHalf dhHalf = new DHHalf(g, p, new byte[]{1, 2, 3});
        return dhHalf.encrypt(keyPair.getPublic());
    }

    @BeforeClass
    public static void init() throws NoSuchProviderException, NoSuchAlgorithmException {
        Security.addProvider(new BouncyCastleProvider());
        keyPair = new RSAKeyGenerator().generateKeys(0);
    }

    @Test
    public void sendReceiveCells() throws Exception {
        try (ServerSocket server1 = new ServerSocket(SERVER_PORT_1)) {
            Endpoint endpoint = new Endpoint(InetAddress.getLocalHost(), 10101);
            BigInteger prime1 = DHKeyExchange.generateRelativePrime();
            BigInteger prime2 = DHKeyExchange.generateRelativePrime();

            Socket client1 = new Socket("localhost", SERVER_PORT_1);

            Socket acceptedSocket1 = server1.accept();

            Vector<Cell> receivedCells = new Vector<>();

            CellWorkerFactory cellWorkerFactory = mock(CellWorkerFactory.class);
            when(cellWorkerFactory.createCellWorker(any(ConnectionWorker.class), any(Cell.class), any(Circuit.class))).thenAnswer((InvocationOnMock invocation) -> {
                ConnectionWorker connectionWorker = invocation.getArgumentAt(0, ConnectionWorker.class);
                Cell cell = invocation.getArgumentAt(1, Cell.class);
                Circuit circuit = invocation.getArgumentAt(2, Circuit.class);

                if (circuit == null) {
                    circuit = new Circuit(cell.getCircuitID(), endpoint);
                    connectionWorker.addCircuit(circuit);
                }
                assertEquals(circuit.getCircuitID(), cell.getCircuitID());

                return new CellCollector(cell, circuit, receivedCells);
            });

            ConnectionWorker c1 = new ConnectionWorker(client1, cellWorkerFactory);
            ConnectionWorker c2 = new ConnectionWorker(acceptedSocket1, cellWorkerFactory);

            assertEquals(0, receivedCells.size());

            c2.handleCell(new CreateCell((short)10, endpoint, createEncryptedDHHalf()));
            c1.sendCell(new CreateCell((short)10, endpoint, createEncryptedDHHalf()));
            c1.sendCell(new CreateCell((short)10, endpoint, createEncryptedDHHalf()));
            Thread.sleep(100);

            assertEquals(3, receivedCells.size());
            assertEquals((short)10, receivedCells.elementAt(1).getCircuitID());

            receivedCells.clear();
            assertEquals(0, receivedCells.size());

            c2.handleCell(new CreateCell((short)20, endpoint, createEncryptedDHHalf()));
            c1.sendCell(new CreateCell((short)20, endpoint, createEncryptedDHHalf()));
            c1.sendCell(new CreateCell((short)20, endpoint, createEncryptedDHHalf()));
            Thread.sleep(100);

            assertEquals(3, receivedCells.size());
            assertEquals((short)20, receivedCells.elementAt(1).getCircuitID());

            c1.close();
            c2.close();
        }
    }

    private static class CellCollector implements CellWorker {
        private Vector<Cell> receivedCells;
        private Cell cell;
        private Circuit circuit;

        public CellCollector(Cell cell, Circuit circuit, Vector<Cell> receivedCells) {
            this.cell = cell;
            this.circuit = circuit;
            this.receivedCells = receivedCells;
        }

        @Override
        public void run() {
            receivedCells.add(cell);
        }
    }
}
