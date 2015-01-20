package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.Cell;
import at.ac.tuwien.aic.ws14.group2.onion.shared.Configuration;
import at.ac.tuwien.aic.ws14.group2.onion.shared.ConfigurationFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.*;

/**
 * Created by Stefan on 02.12.2014.
 */
public class TargetWorker implements AutoCloseable {
    static final Logger logger = LogManager.getLogger(TargetWorker.class.getName());

    private final ConnectionWorker worker;
    private final TargetForwarder forwarder;
    private final NoGapBuffer<Bucket> buffer;
    private final Timer bufferChecker;
    private final ClearBufferTask clearBufferTask;

    public TargetWorker(ConnectionWorker worker, TargetForwarder forwarder) {
        this.worker = worker;
        this.forwarder = forwarder;
        forwarder.setTargetWorkerCallback(this);
        this.buffer = new NoGapBuffer<>((b1, b2) -> Long.compare(b1.getNr(), b2.getNr()), this::allItemsInRange, Integer.toUnsignedLong(-1) /*0xFFFFFFFF*/);
        bufferChecker = new Timer("PeriodicBufferChecker");
        clearBufferTask = new ClearBufferTask();
    }

    public void sendData(byte[] data, long sequenceNumber) {
        Bucket bucket = new Bucket(Arrays.copyOf(data, data.length), sequenceNumber);
        try {
            buffer.add(bucket);
        } catch (BufferOverflowException e) {
            clearBufferTask.run();
            buffer.add(bucket);
        }
    }

    private Set<Bucket> allItemsInRange(Bucket b1, Bucket b2) {
        Set<Bucket> buckets = new HashSet<>();
        if (b1.getNr() <= b2.getNr()) {
            for (long i = b1.getNr() + 1; i < b2.getNr(); i++) {
                buckets.add(new Bucket(new byte[]{}, i));
            }
        } else {
            for (long i = b1.getNr() - 1; i > b2.getNr(); i--) {
                buckets.add(new Bucket(new byte[]{}, i));
            }
        }
        return buckets;
    }

    @Override
    public void close() throws IOException {
        bufferChecker.cancel();
    }

    public void sendCell(Cell cell) {
        try {
            worker.sendCell(cell);
        } catch (IOException e) {
            logger.debug("Unable to send cell " + cell);
            logger.catching(Level.DEBUG, e);
        }
    }

    public TargetForwarder getForwarder() {
        return forwarder;
    }

    public void startForwarding() {
        Configuration configuration = ConfigurationFactory.getConfiguration();
        long targetWorkerTimeout = configuration.getTargetWorkerTimeout();

        bufferChecker.schedule(clearBufferTask, targetWorkerTimeout, targetWorkerTimeout);
    }

    private class ClearBufferTask extends TimerTask {
        @Override
        public void run() {
            Set<Bucket> missingElements = buffer.getMissingElements();
            if (missingElements.size() > 0) {
                logger.fatal("There are some gaps in the input: ");
                logger.fatal("Missing Sequences: " + missingElements.toString());
                //TODO: What should we do here?
                return;
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buffer.getContents().stream()
                    .forEach(bucket -> {
                        try {
                            bos.write(bucket.getData());
                        } catch (IOException e) {
                            logger.catching(Level.DEBUG, e);
                        }
                    });
            buffer.clear();
            try {
                forwarder.forward(bos.toByteArray());
            } catch (IOException e) {
                logger.catching(Level.DEBUG, e);
            }
        }
    }
}
