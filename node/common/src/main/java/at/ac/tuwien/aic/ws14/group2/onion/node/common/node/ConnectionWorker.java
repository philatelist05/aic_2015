package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.Cell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.CircuitIDExistsAlreadyException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.WrongCircuitIDException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Target;
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
    private OutputStream outputStream;
    private InputStream inputStream;

    private ConcurrentHashMap<Short, Circuit> circuits = new ConcurrentHashMap<>();             // circuit ID to circuit
    private ConcurrentHashMap<Short, TargetWorker> targetWorkers = new ConcurrentHashMap<>();   // circuit ID to target worker

    private CellWorkerFactory cellWorkerFactory;

    private Thread cellReceiverThread;
    private ExecutorService cellWorkerPool = Executors.newCachedThreadPool();

    /**
     * @param socket Takes ownership of this socket.
     */
    public ConnectionWorker(Socket socket, CellWorkerFactory cellWorkerFactory) throws IOException {
        this.socket = socket;
        this.outputStream = socket.getOutputStream();
        this.inputStream = socket.getInputStream();
        this.cellWorkerFactory = cellWorkerFactory;

        cellReceiverThread = new Thread(new CellReceiver(inputStream));
        cellReceiverThread.start();
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
        synchronized (this.outputStream) {
            cell.send(this.outputStream);
        }
    }

    /**
     * Adds the specified circuit to this connection worker if its ID does not exist yet.
     * @throws CircuitIDExistsAlreadyException Thrown if the circuit's ID already exists on this connection.
     */
    public void addCircuit(Circuit circuit) throws CircuitIDExistsAlreadyException {
        if (circuits.putIfAbsent(circuit.getCircuitID(), circuit) != null)
            throw new CircuitIDExistsAlreadyException("Cannot create two circuits with the same ID on the same connection.");
    }

    /**
     * Removes the specified circuit or does nothing if it does not exist.
     */
    public void removeCircuit(Circuit circuit) {
        circuits.remove(circuit.getCircuitID());
    }

    /**
     * Creates a new TargetWorker for the specified circuit and endpoint.
     * @throws CircuitIDExistsAlreadyException Thrown if there is already a TargetWorker for the specified circuit.
     */
    public void createTargetWorker(Circuit incomingCircuit, Endpoint target) throws CircuitIDExistsAlreadyException, IOException {
        TargetWorker worker = new TargetWorker(this, new SocketForwarder(target));
        if (targetWorkers.putIfAbsent(incomingCircuit.getCircuitID(), worker) != null) {
            worker.close();

            throw new CircuitIDExistsAlreadyException("Only one target worker allowed for a single chain.");
        }
    }

    public TargetWorker getTargetWorker(Circuit incomingCircuit) {
        return targetWorkers.get(incomingCircuit);
    }

    /**
     * Closes a target worker and removes it from the list or does nothing if there is no target worker for the specified circuit.
     */
    public void removeTargetWorker(Circuit incomingCircuit) {
        try {
            TargetWorker targetWorker = targetWorkers.remove(incomingCircuit.getCircuitID());
            if (targetWorker != null)
                targetWorker.close();
        } catch (IOException e) {
            logger.error("Exception when closing the target worker.", e);
        }
    }

    /**
     * Handles a cell that has been received by this connection.
     * @param cell The incoming cell that is handled.
     */
    public void handleCell(Cell cell) {
        Circuit circuit;

        // get circuit for received cell
        circuit = circuits.get(cell.getCircuitID());

        // process cell
        cellWorkerPool.execute(cellWorkerFactory.createCellWorker(this, cell, circuit));
    }

    /**
     * Creates a new circuit and adds it to this connection worker.
     * The new circuit is guaranteed to have a unique ID on this connection.
     */
    public Circuit createAndAddCircuit(Endpoint endpoint) {
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
        private final InputStream inputStream;

        public CellReceiver(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    handleCell(Cell.receive(this.inputStream));
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
