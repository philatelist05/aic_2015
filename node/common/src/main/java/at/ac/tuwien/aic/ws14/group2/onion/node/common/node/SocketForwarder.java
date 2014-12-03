package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.Cell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.DataCommand;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.RelayCell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.RelayCellPayload;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.EncryptException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.SocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Stefan on 02.12.14.
 */
public class SocketForwarder extends Thread implements TargetForwarder, AutoCloseable {
    static final Logger logger = LogManager.getLogger(TargetWorker.class.getName());

    private final SocketFactory socketFactory;
    private InetAddress address;
    private int port;
    private Socket socket;
    private OutputStream outputStream;
    private  InputStream inputStream;
    private final Circuit circuit;
    private TargetWorker targetWorker;
    private boolean stop = false;
    private short lastUsedSequenceNumber = 0;


    /**
     * Creates a SocketForwarder which is not connected to the target yet.
     */
    public SocketForwarder(Circuit circuit, SocketFactory socketFactory) throws IOException {
        super();
        this.circuit = circuit;
        this.socketFactory = socketFactory;
    }

    /**
     * Connects this forwarder to the specified target and starts this thread.
     */
    @Override
    public void connect(Endpoint endpoint) throws IOException {

        // TODO Make this method thread-safe? If two ConnectCommands arrive, it may be called twice at the same time.

        address = endpoint.getAddress();
        port = endpoint.getPort();
        socket = socketFactory.createSocket(address, port);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        this.start();
    }

    /**
     * Must be called after connectTo.
     */
    @Override
    public void forward(byte[] data) throws IOException {
        outputStream.write(data);
    }

    @Override
    public void setTargetWorkerCallback(TargetWorker targetWorker) {
        this.targetWorker = targetWorker;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[DataCommand.MAX_DATA_LENGTH];

        try {
            while (!stop) {
                try {
                    int actualBytesRead = inputStream.read(buffer);

                    if (actualBytesRead < 1) {
                        if (actualBytesRead < 0)
                            stop = true;
                        continue;
                    }
                    Cell cell = encryptCommandForChain(Arrays.copyOf(buffer, actualBytesRead));

                    if (targetWorker != null)
                        targetWorker.sendCell(cell);
                } catch (EncryptException | DecodeException e) {
                    logger.debug("Unable to encrypt cell");
                    logger.catching(Level.DEBUG, e);
                }
            }
        } catch (IOException e) {
            // The if-clause down here is because of what is described in
            // http://docs.oracle.com/javase/6/docs/technotes/guides/concurrency/threadPrimitiveDeprecation.html
            // under "What if a thread doesn't respond to Thread.interrupt?"

            // So the socket is closed when the ClientNetworkService is
            // closed and in that special case no error should be
            // propagated.
            if (!stop) {
                Thread.UncaughtExceptionHandler eh = this.getUncaughtExceptionHandler();
                if (eh != null)
                    eh.uncaughtException(this, e);
                else
                    logger.error("Uncaught exception in thread: " + this.getName(), e);
            }
        }
    }

    private Cell encryptCommandForChain(byte[] payload) throws EncryptException, DecodeException {
        DataCommand command = new DataCommand(payload);
        command.setSequenceNumber(lastUsedSequenceNumber++);
        RelayCellPayload relayCellPayload = new RelayCellPayload(command);
        relayCellPayload.encrypt(circuit.getSessionKey());
        return new RelayCell(circuit.getCircuitID(), relayCellPayload);
    }

    @Override
    public void close() throws Exception {
        stop = true;
        this.interrupt();
        this.socket.close();
    }
}
