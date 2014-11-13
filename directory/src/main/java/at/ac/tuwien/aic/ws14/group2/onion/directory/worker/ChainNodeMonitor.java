package at.ac.tuwien.aic.ws14.group2.onion.directory.worker;

import at.ac.tuwien.aic.ws14.group2.onion.directory.ChainNodeRegistry;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsageSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

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

        Map<ChainNodeInformation, NodeUsageSummary> activeNodes = chainNodeRegistry.getActiveStatistics();

        if (activeNodes == null || activeNodes.isEmpty()) {
            logger.warn("No active ChainNodes, nothing to do..");
            logger.info("Finished health check");
            return;
        }

        for (Map.Entry<ChainNodeInformation, NodeUsageSummary> entry : activeNodes.entrySet()) {
            NodeUsageSummary usageSummary = entry.getValue();
            if (usageSummary != null) {
                try {
                    // TODO discuss whether to switch to JodaTime for Date/Time handling
                    Date then = DateFormat.getDateTimeInstance().parse(usageSummary.getEndTime());
                    Calendar cutoff = Calendar.getInstance();
                    cutoff.add(Calendar.MILLISECOND, -this.timeout);
                    if (then.before(cutoff.getTime())) {
                        chainNodeRegistry.deactivate(entry.getKey());
                    }
                } catch (ParseException e) {
                    logger.warn("Cannot parse endDate '{}' of NodeUsageSummary for ChainNode '{}'", usageSummary.getEndTime(), entry.getKey());
                    logger.debug(e.getStackTrace());
                }
            }
        }

        logger.info("Finished health check");
    }
}
