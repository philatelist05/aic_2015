package at.ac.tuwien.aic.ws14.group2.onion.directory.worker;

import at.ac.tuwien.aic.ws14.group2.onion.directory.ChainNodeRegistry;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

public class ChainNodeMonitor implements Runnable {
    static final Logger logger = LogManager.getLogger(ChainNodeMonitor.class.getName());

    private ChainNodeRegistry chainNodeRegistry;
    private int timeout;

    public ChainNodeMonitor(ChainNodeRegistry chainNodeRegistry, int timeout) {
        this.chainNodeRegistry = chainNodeRegistry;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        logger.info("Starting health check");

        Set<ChainNodeInformation> activeNodes = chainNodeRegistry.getActiveNodes();

        if (activeNodes == null || activeNodes.isEmpty()) {
            logger.warn("No active ChainNodes, nothing to do..");
            logger.info("Finished health check");
            return;
        } else {
            logger.info("Found {} active nodes.", activeNodes.size());
        }

        for (ChainNodeInformation nodeInformation : activeNodes) {
            NodeUsage usage = chainNodeRegistry.getLastNodeUsage(nodeInformation);
            if (usage != null) {
                try {
                    LocalDateTime then = LocalDateTime.parse(usage.getEndTime(), DateTimeFormatter.ISO_DATE_TIME);
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime cutoff = now.minus(timeout, ChronoUnit.MILLIS);
                    if (then.until(cutoff, ChronoUnit.MILLIS) < 0) {
                        chainNodeRegistry.deactivate(nodeInformation);
                    }
                } catch (DateTimeParseException e) {
                    logger.warn("Cannot parse endDate '{}' of NodeUsageSummary for ChainNode '{}'", usage.getEndTime(), nodeInformation);
                    logger.debug(e.getStackTrace());
                }
            }
        }

        logger.info("Finished health check");
    }
}
