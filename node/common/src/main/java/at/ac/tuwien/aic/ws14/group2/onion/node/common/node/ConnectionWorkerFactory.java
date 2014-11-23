package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.ConnectionWorkerAlreadyExistsException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.ConnectionWorkerException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Thomas on 22.11.2014.
 *
 * Instances of this class are thread-safe.
 */
public class ConnectionWorkerFactory {
    private static final Logger logger = LogManager.getLogger(ConnectionWorkerFactory.class);

    private static ConnectionWorkerFactory instance;

    private ConcurrentHashMap<Endpoint, ConnectionWorker> connectionWorkers = new ConcurrentHashMap<>();
    private CellWorkerFactory cellWorkerFactory;

    private ConnectionWorkerFactory(CellWorkerFactory cellWorkerFactory) {
        this.cellWorkerFactory = cellWorkerFactory;
    }

    public static void setCellWorkerFactory(CellWorkerFactory cellWorkerFactory) {
        if (instance != null) {
            logger.error("Cannot set CellWorkerFactory multiple times.");
            return;
        }

        instance = new ConnectionWorkerFactory(cellWorkerFactory);
    }

    /**
     * Returns a singleton instance of this class.
     * Requires setCellWorkerFactory to be called beforehand.
     */
    public static ConnectionWorkerFactory getInstance() {
        return instance;
    }

    /**
     * Opens a connection to the specified node and returns a new ConnectionWorker handling that connection.
     * Creates a new worker iff there is no one for the specified node yet, otherwise returns the existing worker.
     */
    public ConnectionWorker getConnectionWorker(Endpoint endpoint) throws IOException {
        ConnectionWorker worker = connectionWorkers.get(endpoint);
        if (worker == null)
            worker = createOrUseExisting(endpoint);

        return worker;
    }

    private ConnectionWorker createOrUseExisting(Endpoint endpoint) throws IOException {
        Socket socket = new Socket(endpoint.getAddress(), endpoint.getPort());
        ConnectionWorker worker = new ConnectionWorker(socket, cellWorkerFactory);

        ConnectionWorker existingWorker = connectionWorkers.putIfAbsent(endpoint, worker);
        if (existingWorker == null) {
            return worker;
        } else {
            worker.close();
            return existingWorker;
        }
    }

    /**
     * Creates a ConnectionWorker for the specified socket.
     *
     * @param endpoint The endpoint of this connection (address + listening port of other node).
     * @param socket The socket that was created when the incoming connection was accepted.
     * @exception at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.ConnectionWorkerAlreadyExistsException Thrown if there is already a ConnectionWorker for the specified connection.
     */
    public ConnectionWorker createIncomingConnectionWorker(Endpoint endpoint, Socket socket) throws ConnectionWorkerAlreadyExistsException, ConnectionWorkerException {
        ConnectionWorker worker = null;
        try {
            worker = new ConnectionWorker(socket, cellWorkerFactory);
        } catch (IOException e) {
            logger.warn("Encountered IOException while creating new ConnectionWorker: {}", e.getMessage());
            throw new ConnectionWorkerException();
        }
        if (connectionWorkers.putIfAbsent(endpoint, worker) != null)
            throw new ConnectionWorkerAlreadyExistsException("There is already a connection worker for node " + endpoint + ".");

        return worker;
    }
}
