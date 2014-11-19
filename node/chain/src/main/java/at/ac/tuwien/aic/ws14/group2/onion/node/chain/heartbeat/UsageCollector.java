package at.ac.tuwien.aic.ws14.group2.onion.node.chain.heartbeat;

import java.util.concurrent.atomic.AtomicLong;

public class UsageCollector {
    protected static AtomicLong currentRelayMsgCount = new AtomicLong(0);
    protected static AtomicLong currentCreateMsgCount = new AtomicLong(0);

    public static void incrementRelayCounter() {
        currentRelayMsgCount.incrementAndGet();
    }

    public static void incrementCreateCounter() {
        currentCreateMsgCount.incrementAndGet();
    }
}
