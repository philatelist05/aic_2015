package at.ac.tuwien.aic.ws14.group2.onion.directory;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsageSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;


public class ChainNodeRegistry {
    static final Logger logger = LogManager.getLogger(ChainNodeRegistry.class.getName());
    private ConcurrentHashMap<ChainNodeInformation, ConcurrentLinkedDeque<NodeUsage>> nodeUsages;
    private ConcurrentSkipListSet<ChainNodeInformation> activeNodes;
    private ConcurrentSkipListSet<ChainNodeInformation> inactiveNodes;

    public ChainNodeRegistry() {
        logger.info("Initializing ChainNodeRegistry");
        this.activeNodes = new ConcurrentSkipListSet<ChainNodeInformation>();
        this.inactiveNodes = new ConcurrentSkipListSet<ChainNodeInformation>();
        this.nodeUsages = new ConcurrentHashMap<ChainNodeInformation, ConcurrentLinkedDeque<NodeUsage>>();
    }

    //TODO check signature?
    public boolean addNodeUsage(ChainNodeInformation chainNodeInformation, NodeUsage usage) {
        logger.info("Recording NodeUsage for ChainNode '{}'", chainNodeInformation);
        logger.debug("Content of NodeUsage: '{}'", usage);

        ConcurrentLinkedDeque usages = nodeUsages.get(chainNodeInformation);

        if (usages == null) {
            //TODO Exception
            logger.warn("Cannot record NodeUsage for unknown ChainNode '{}'", chainNodeInformation);
            return false;
        } else {
            usages.addLast(usage);
            if (!activeNodes.contains(chainNodeInformation)) {
                activate(chainNodeInformation);
            }
            return true;
        }
    }

    public boolean addNewChainNode(ChainNodeInformation chainNodeInformation) {
        logger.info("Adding new ChainNode '{}'", chainNodeInformation);

        ConcurrentLinkedDeque<NodeUsage> usages = new ConcurrentLinkedDeque<>();
        ConcurrentLinkedDeque<NodeUsage> existingUsages = nodeUsages.putIfAbsent(chainNodeInformation, usages);

        if (existingUsages != null)
            activate(chainNodeInformation);

        return existingUsages == null;
    }

    public void activate(ChainNodeInformation chainNodeInformation) {
        logger.info("Activating ChainNode '{}'", chainNodeInformation);

        synchronized (activeNodes) {
            inactiveNodes.remove(chainNodeInformation);
            activeNodes.add(chainNodeInformation);
        }
    }

    public void deactivate(ChainNodeInformation chainNodeInformation) {
        logger.info("Deactivating ChainNode '{}'", chainNodeInformation);

        synchronized (activeNodes) {
            activeNodes.remove(chainNodeInformation);
            inactiveNodes.add(chainNodeInformation);
        }
    }

    public Map<ChainNodeInformation, NodeUsageSummary> getActiveStatistics() {
        //TODO implement - calculate summaries here?
        return null;
    }

    public Set<ChainNodeInformation> getActiveNodes() {
        logger.info("Returning active ChainNodes");
        return activeNodes;
    }

    public NodeUsage getLastNodeUsage(ChainNodeInformation nodeInformation) {
        ConcurrentLinkedDeque<NodeUsage> usages = nodeUsages.get(nodeInformation);
        return usages == null ? null : usages.getLast();
    }
}
