package at.ac.tuwien.aic.ws14.group2.onion.node.chain.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.Cell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.CreateCell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.*;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorker;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorkerFactory;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChainNodeCore implements Runnable {
    static final Logger logger = LogManager.getLogger(ChainNodeCore.class.getName());

    private ServerSocket serverSocket;
    private ExecutorService incomingWorkerPool;

    public ChainNodeCore(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.incomingWorkerPool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        while (true) {
            logger.info("Listening on port {}", serverSocket.getLocalPort());
            if (Thread.interrupted()) {
                logger.info("Got interrupted, exiting accept()-loop");
                break;
            }
            try {
                Socket socket = serverSocket.accept();
                incomingWorkerPool.submit(new IncomingWorker(socket));
            } catch (IOException e) {
                logger.warn("Caught IOException while accepting on listening Socket: ", e.getMessage());
                continue;
            }
        }
    }

    private class IncomingWorker implements Runnable {

        private Socket socket;

        private IncomingWorker(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                Cell cell = Cell.receive(socket.getInputStream());
                if (cell instanceof CreateCell) {
                    CreateCell createCell = (CreateCell) cell;
                    ConnectionWorker connectionWorker = ConnectionWorkerFactory.getInstance().createIncomingConnectionWorker(createCell.getEndpoint(), socket);
                    connectionWorker.handleCell(createCell);
                } else {
                    logger.warn("Dropping non-Create cell on listening socket: {}", cell.toString());
                }
            } catch (IOException e) {
                logger.warn("Caught IOException while reading from incoming Socket: ", e.getMessage());
            } catch (DecodeException e) {
                logger.warn("Caught DecodeException while decoding on incoming Socket: ", e.getMessage());
            } catch (ConnectionWorkerAlreadyExistsException e) {
                logger.warn("Caught ConnectionWorkerAlreadyExistsException while decoding on incoming Socket: ", e.getMessage());
                //TODO send ErrorCell
            } catch (ConnectionWorkerException e) {
                logger.warn("Caught ConnectionWorkerException while decoding on incoming Socket: ", e.getMessage());
                //TODO send ErrorCell
            }
        }
    }
}
