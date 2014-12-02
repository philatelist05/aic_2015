package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import at.ac.tuwien.aic.ws14.group2.onion.shared.Configuration;
import at.ac.tuwien.aic.ws14.group2.onion.shared.ConfigurationFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.*;

/**
 * Created by Thomas on 22.11.2014.
 */
public class TargetWorker implements AutoCloseable {
    static final Logger logger = LogManager.getLogger(TargetWorker.class.getName());

    private final ConnectionWorker worker;
    private final short circuitID;
    private final TargetForwarder forwarder;
    private final NoGapBuffer<Bucket> buffer;
    private final Timer bufferChecker;
    private final ClearBufferTask clearBufferTask;

    public TargetWorker(ConnectionWorker worker, short circuitID, TargetForwarder forwarder) {
        Configuration configuration = ConfigurationFactory.getConfiguration();
        this.worker = worker;
        this.circuitID = circuitID;
        this.forwarder = forwarder;
        this.buffer = new NoGapBuffer<>((b1, b2) -> Short.compare(b1.nr, b2.nr), this::allItemsInRange, Short.MAX_VALUE);
        bufferChecker = new Timer("PeriodicBufferChecker");
        clearBufferTask = new ClearBufferTask();
        bufferChecker.schedule(clearBufferTask, configuration.getTargetWorkerTimeout());
    }

    public void sendData(byte[] data, short sequenceNumber) {
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
        if (b1.nr <= b2.nr) {
            for (int i = b1.nr + 1; i < b2.nr; i++) {
                buckets.add(new Bucket(new byte[]{}, (short)i));
            }
        } else {
            for (int i = b1.nr - 1; i > b2.nr ; i--) {
                buckets.add(new Bucket(new byte[]{}, (short)i));
            }
        }
        return buckets;
    }

    @Override
    public void close() throws IOException {
        bufferChecker.cancel();
    }

    private class Bucket {
        private byte[] data;
        private short nr;

        private Bucket(byte[] data, short sequenceNumber) {
            this.data = data;
            this.nr = sequenceNumber;
        }

        @Override
        public String toString() {
            return "Bucket[" +
                    "data=" + Arrays.toString(data) +
                    ", nr=" + nr +
                    ']';
        }
    }

    private class ClearBufferTask extends TimerTask {
        @Override
        public void run() {
            Set<Bucket> missingElements = buffer.getMissingElements();
            if (missingElements.size() > 0) {
                logger.fatal("There are some gaps in the input: ");
                logger.fatal("Missing Sequences: " + missingElements.toString());
                //TODO: What should we do here?
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buffer.getContents().stream()
                    .map(bucket -> {
                        try {
                            bos.write(bucket.data);
                        } catch (IOException ignored) {
                            //That Exception can safely be ignored
                            //because it will never get thrown
                        }
                        return bucket;
                    });
            buffer.clear();
            forwarder.forward(bos.toByteArray());
        }
    }
}
