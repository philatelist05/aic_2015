package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.Cell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.CircuitIDExistsAlreadyException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.WrongCircuitIDException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.*;

/**
 * Created by Thomas on 22.11.2014.
 */
public class ConnectionWorker implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger(ConnectionWorker.class);

    private Socket socket;

    private ConcurrentHashMap<Short, Circuit> circuits = new ConcurrentHashMap<>();             // circuit ID to circuit
    private ConcurrentHashMap<Short, TargetWorker> targetWorkers = new ConcurrentHashMap<>();   // circuit ID to target worker

    private CellWorkerFactory cellWorkerFactory;

    private Thread cellReceiverThread;
    private ExecutorService cellWorkerPool = Executors.newCachedThreadPool();

    /**
     * @param socket Takes ownership of this socket.
     */
    public ConnectionWorker(Socket socket, CellWorkerFactory cellWorkerFactory) {
        this.socket = socket;

        cellReceiverThread = new Thread(new CellReceiver());
        cellReceiverThread.start();

        this.cellWorkerFactory = cellWorkerFactory;
    }

    /**
     * Closes the connection and releases all resources managed by this instance.
     */
    @Override
    public void close() throws IOException {
        try {
            socket.close();   // forces the receiver thread to quit

            cellReceiverThread.join();

            cellWorkerPool.shutdown();
            cellWorkerPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("Thread interrupted while closing connection worker.", e);
        }
    }

    public void sendCell(Cell cell) throws IOException {
        cell.send(socket.getOutputStream());
    }

    /**
     * Adds the specified circuit to this connection worker if its ID does not exist yet.
     * @throws CircuitIDExistsAlreadyException Thrown if the circuit's ID already exists on this connection.
     */
    public void addCircuit(Circuit circuit) throws CircuitIDExistsAlreadyException {
        if (circuits.putIfAbsent(circuit.getCircuitID(), circuit) != null)
            throw new CircuitIDExistsAlreadyException("Cannot create two circuits with the same ID on the same connection.");
    }

    public void createTargetWorker(Circuit incomingCircuit, Endpoint target) throws CircuitIDExistsAlreadyException, IOException {
        TargetWorker worker = new TargetWorker(this, target);
        if (targetWorkers.putIfAbsent(incomingCircuit.getCircuitID(), worker) != null) {
            worker.close();

            throw new CircuitIDExistsAlreadyException("Only one target worker allowed for a single chain.");
        }
    }

    /**
     * Handles a cell that has been received by this connection and creates a circuit for that cell.
     */
    public void handleCellAndCreateCircuit(Cell cell) throws WrongCircuitIDException, CircuitIDExistsAlreadyException {
        handleCell(cell, false);
    }

    /**
     * Handles a cell that has been received by this connection.
     * @param useExistingCircuit True if the cell belongs to an existing circuit.
     *                           False if a new circuit should be created for the cell. This is necessary for the very first cell.
     */
    private void handleCell(Cell cell, boolean useExistingCircuit) throws WrongCircuitIDException, CircuitIDExistsAlreadyException {
        Circuit circuit;

        // get circuit for received cell
        if (useExistingCircuit) {
            circuit = circuits.get(cell.getCircuitID());

            if (circuit == null)
                throw new WrongCircuitIDException("Cell received, which references a non-existing circuit.");
        } else {
            InetAddress remoteAddress = socket.getInetAddress();
            int remotePort = socket.getPort();

            circuit = new Circuit(cell.getCircuitID(), new Endpoint(remoteAddress, remotePort));
            addCircuit(circuit);
        }

        // process cell
        cellWorkerPool.execute(cellWorkerFactory.createCellWorker(cell, circuit));
    }

    /**
     * Creates a new circuit and adds it to this connection worker.
     * The new circuit is guaranteed to have a unique ID on this connection.
     */
    private Circuit createAndAddCircuit(Endpoint endpoint) {
        boolean success = false;
        Circuit circuit = null;

        while (!success) {
            // create circuit with random ID
            circuit = new Circuit(endpoint);

            success = circuits.putIfAbsent(circuit.getCircuitID(), circuit) == null;
        }

        return circuit;
    }

    /**
     * Receives cells from the other node and hands them over to the cell workers.
     */
    private class CellReceiver implements Runnable {
        @Override
        public void run() {
            try {
                InputStream stream = socket.getInputStream();
                while (true) {
                    handleCell(Cell.receive(stream), true);
                }
            } catch (SocketException e) {
                logger.info("Connection closed.");
            } catch (EOFException e) {
                logger.info("Connection closed.");
            } catch (Exception e) {
                logger.error("ConnectionWorker thread terminated.", e);
            }
        }
    }
}
