package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Thomas on 22.11.2014.
 */
public class TargetWorker implements AutoCloseable, Runnable {
    static final Logger logger = LogManager.getLogger(TargetWorker.class.getName());

    private final ConnectionWorker worker;
    private final Endpoint endpoint;
    private final NoGapSkipListSet<Bucket> buffer;

    public TargetWorker(ConnectionWorker worker, Endpoint endpoint) {
        this.worker = worker;
        this.endpoint = endpoint;
        this.buffer = new NoGapSkipListSet<>((b1, b2) -> Short.compare(b1.nr, b2.nr), this::allItemsInRange);
    }

    public void sendData(byte[] data, short sequenceNumber) {
        Bucket bucket = new Bucket(data, sequenceNumber);
        buffer.add(bucket);
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
    public void run() {
//        worker.sendCell(new RelayCell(0, new RelayCellPayload(new byte[]{})));
    }

    @Override
    public void close() throws IOException {
        // TODO
    }

    private class Bucket {
        private byte[] data;
        private short nr;

        private Bucket(byte[] data, short sequenceNumber) {
            this.data = data;
            this.nr = sequenceNumber;
        }
    }
}
