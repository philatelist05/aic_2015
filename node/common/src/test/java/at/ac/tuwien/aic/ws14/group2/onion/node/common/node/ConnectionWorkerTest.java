package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.Cell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.CreateCell;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Thomas on 23.11.2014.
 */
public class ConnectionWorkerTest {

    private static final int SERVER_PORT_1 = 10100;

    @Test
    public void sendReceiveCells() throws Exception {
        try (ServerSocket server1 = new ServerSocket(SERVER_PORT_1)) {
            Socket client1 = new Socket("localhost", SERVER_PORT_1);

            Socket acceptedSocket1 = server1.accept();

            Vector<Cell> receivedCells = new Vector<>();

            CellWorkerFactory cellWorkerFactory = mock(CellWorkerFactory.class);
            when(cellWorkerFactory.createCellWorker(any(Cell.class), any(Circuit.class))).thenAnswer((InvocationOnMock invocation) -> {
                Cell cell = invocation.getArgumentAt(0, Cell.class);
                Circuit circuit = invocation.getArgumentAt(1, Circuit.class);

                assertEquals(circuit.getCircuitID(), cell.getCircuitID());

                return new CellCollector(cell, circuit, receivedCells);
            });

            ConnectionWorker c1 = new ConnectionWorker(client1, cellWorkerFactory);
            ConnectionWorker c2 = new ConnectionWorker(acceptedSocket1, cellWorkerFactory);

            assertEquals(0, receivedCells.size());

            c2.handleCellAndCreateCircuit(new CreateCell((short)10, new byte[]{1}));
            c1.sendCell(new CreateCell((short)10, new byte[] {2}));
            c1.sendCell(new CreateCell((short)10, new byte[] {3}));
            Thread.sleep(100);

            assertEquals(3, receivedCells.size());
            assertEquals((short)10, receivedCells.elementAt(1).getCircuitID());

            receivedCells.clear();
            assertEquals(0, receivedCells.size());

            c2.handleCellAndCreateCircuit(new CreateCell((short)20, new byte[]{1}));
            c1.sendCell(new CreateCell((short)20, new byte[] {2}));
            c1.sendCell(new CreateCell((short)20, new byte[] {3}));
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
