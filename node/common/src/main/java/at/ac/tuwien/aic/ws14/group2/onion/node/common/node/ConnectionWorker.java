package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.Cell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.CircuitIDExistsAlreadyException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.SocketFactory;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Thomas on 22.11.2014.
 */
public class ConnectionWorker implements AutoCloseable {
    private final Logger logger;

    private final Socket socket;
    private final OutputStream outputStream;
    private final InputStream inputStream;

    private final ConcurrentHashMap<Short, Circuit> circuits = new ConcurrentHashMap<>();             // circuit ID to circuit
    private final ConcurrentHashMap<Short, TargetWorker> targetWorkers = new ConcurrentHashMap<>();   // circuit ID to target worker

    private final CellWorkerFactory cellWorkerFactory;

    private final Thread cellReceiverThread;
    private final ExecutorService cellWorkerPool = Executors.newCachedThreadPool();

    /**
     * @param socket Takes ownership of this socket.
     */
    public ConnectionWorker(Endpoint endpoint, Socket socket, CellWorkerFactory cellWorkerFactory) throws IOException {
        this.logger = LogManager.getLogger(ConnectionWorker.class.getCanonicalName() + "[" + endpoint.getAddress().getHostAddress() + ":" + endpoint.getPort() + "]");
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
        logger.info("close() called");
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
     * Gets an existing TargetWorker for the specified circuit or creates a new one.
     * @param target Can be null. If it is not null the TargetWorker will be connected to this endpoint.
     */
    public TargetWorker getOrCreateTargetWorker(Circuit incomingCircuit, Endpoint target) throws IOException {
        // In most cases there is already a target worker.
        TargetWorker targetWorker = targetWorkers.get(incomingCircuit.getCircuitID());
        if (targetWorker != null) {
            if (target != null) {
                logger.debug("Connect forwarder to {}", target);
                targetWorker.getForwarder().connect(target);
            }
            return targetWorker;
        }

        // If there is none, create one.
        SocketForwarder forwarder = new SocketForwarder(incomingCircuit, SocketFactory.getDefault(), null);
        targetWorker = new TargetWorker(this, forwarder);
        TargetWorker oldWorker = targetWorkers.putIfAbsent(incomingCircuit.getCircuitID(), targetWorker);
        if (oldWorker != null) {
            targetWorker.close();

            // race condition --> return existing TargetWorker
            if (target != null) {
                logger.debug("Connect forwarder to {}", target);
                oldWorker.getForwarder().connect(target);
            }
            return oldWorker;
        } else {
            if (target != null) {
                logger.debug("Connect forwarder to {}", target);
                targetWorker.getForwarder().connect(target);
            }
            return targetWorker;
        }
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

    public int getCircuitCount() {
        return circuits.size();
    }

    public int getTargetWorkerCount() {
        return targetWorkers.size();
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
                logger.info("Connection closed (SocketException): {}", e.getMessage());
            } catch (EOFException e) {
                logger.info("Connection closed (EOF): {}", e.getMessage());
            } catch (Exception e) {
                logger.error("ConnectionWorker thread terminated.", e);
            }
        }
    }
}
