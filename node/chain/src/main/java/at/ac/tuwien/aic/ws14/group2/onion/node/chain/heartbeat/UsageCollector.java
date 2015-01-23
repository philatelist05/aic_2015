package at.ac.tuwien.aic.ws14.group2.onion.node.chain.heartbeat;

import java.util.concurrent.atomic.AtomicLong;

public class UsageCollector {
    protected static AtomicLong currentRelayMsgCount = new AtomicLong(0);
    protected static AtomicLong currentCreateMsgCount = new AtomicLong(0);
    protected static AtomicLong circuitCount = new AtomicLong(0);
    protected static AtomicLong chainCount = new AtomicLong(0);
    protected static AtomicLong targetCount = new AtomicLong(0);

    public static void incrementRelayCounter() {
        currentRelayMsgCount.incrementAndGet();
    }

    public static void incrementCreateCounter() {
        currentCreateMsgCount.incrementAndGet();
    }

    public static void setCircuitCount(long count) {
        circuitCount.set(count);
    }

    public static void setTargetCount(long count) {
        targetCount.set(count);
    }

    public static void incrementChainCounter() {
        chainCount.incrementAndGet();
    }

    public static void decrementChainCounter() {
        chainCount.decrementAndGet();
    }
}
