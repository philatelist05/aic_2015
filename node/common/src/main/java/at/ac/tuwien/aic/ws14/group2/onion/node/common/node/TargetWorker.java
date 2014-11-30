package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.RelayCell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.RelayCellPayload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Created by Thomas on 22.11.2014.
 */
public class TargetWorker implements AutoCloseable, Runnable {
    static final Logger logger = LogManager.getLogger(TargetWorker.class.getName());

    private final ConnectionWorker worker;
    private final Endpoint endpoint;

    public TargetWorker(ConnectionWorker worker, Endpoint endpoint) {
        this.worker = worker;
        this.endpoint = endpoint;
    }

    public void sendData(byte[] data, short sequenceNumber) {

    }

    @Override
    public void run() {
//        worker.sendCell(new RelayCell(0, new RelayCellPayload(new byte[]{})));
    }

    @Override
    public void close() throws IOException {
        // TODO
    }
}
