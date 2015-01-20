package at.ac.tuwien.aic.ws14.group2.onion.directory.worker;

import at.ac.tuwien.aic.ws14.group2.onion.directory.ChainNodeRegistry;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Set;

public class ChainNodeMonitor implements Runnable {
    static final Logger logger = LogManager.getLogger(ChainNodeMonitor.class.getName());

    private ChainNodeRegistry chainNodeRegistry;
    private long timeout;

    public ChainNodeMonitor(ChainNodeRegistry chainNodeRegistry, long timeout) {
        this.chainNodeRegistry = chainNodeRegistry;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        logger.info("Starting health check");

        Set<Integer> activeNodes = chainNodeRegistry.getActiveNodeIDs();

        if (activeNodes == null || activeNodes.isEmpty()) {
            logger.warn("No active ChainNodes, nothing to do..");
            logger.info("Finished health check");
            return;
        } else {
            logger.info("Found {} active nodes.", activeNodes.size());
        }

        for (Integer nodeID : activeNodes) {
            NodeUsage usage = chainNodeRegistry.getLastNodeUsage(nodeID);
            if (usage != null) {
                try {
                    LocalDateTime then = LocalDateTime.parse(usage.getEndTime(), DateTimeFormatter.ISO_DATE_TIME);
                    LocalDateTime now = LocalDateTime.now();
                    if (then.until(now, ChronoUnit.MILLIS) > timeout) {
                        chainNodeRegistry.deactivate(nodeID);
                    }
                } catch (DateTimeParseException e) {
                    logger.warn("Cannot parse endDate '{}' of NodeUsageSummary for ChainNode '{}'", usage.getEndTime(), nodeID);
                    logger.debug(e.getStackTrace());
                } catch (ArithmeticException e) {
                    logger.warn("Overflow occurred while calculation timeout, deactivating..");
                    logger.catching(Level.DEBUG, e);
                    chainNodeRegistry.deactivate(nodeID);
                }
            }
        }

        logger.info("Finished health check");
    }
}
