package at.ac.tuwien.aic.ws14.group2.onion.directory;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsageSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;


public class ChainNodeRegistry {
    static final Logger logger = LogManager.getLogger(ChainNodeRegistry.class.getName());

    private final ConcurrentHashMap<ChainNodeInformation, ConcurrentLinkedDeque<NodeUsage>> nodeUsages;
    private final ConcurrentSkipListSet<ChainNodeInformation> activeNodes;
    private final ConcurrentSkipListSet<ChainNodeInformation> inactiveNodes;
    //TODO: Any better solution here?
    private final ConcurrentHashMap<Integer, ChainNodeInformation> nodeMapping;
    private final AtomicInteger nextNodeID;

    public ChainNodeRegistry() {
        logger.info("Initializing ChainNodeRegistry");
        this.activeNodes = new ConcurrentSkipListSet<>();
        this.inactiveNodes = new ConcurrentSkipListSet<>();
        this.nodeUsages = new ConcurrentHashMap<>();
        this.nodeMapping = new ConcurrentHashMap<>();
        this.nextNodeID = new AtomicInteger();
    }

    //TODO check signature?
    public boolean addNodeUsage(int chainNodeID, NodeUsage usage) {
        logger.debug("Recording NodeUsage for ChainNode '{}': {}", chainNodeID, usage);

        ChainNodeInformation chainNodeInformation = nodeMapping.get(chainNodeID);

        if (chainNodeInformation == null) {
            //TODO Exception
            logger.warn("Cannot record NodeUsage for unknown ID '{}'", chainNodeID);
            return false;
        } else {
            ConcurrentLinkedDeque<NodeUsage> usages = nodeUsages.get(chainNodeInformation);
            usages.addLast(usage);

            if (!activeNodes.contains(chainNodeInformation)) {
                activate(chainNodeInformation);
            }
            return true;
        }
    }

    public int addNewChainNode(ChainNodeInformation chainNodeInformation) {
        logger.info("Adding new ChainNode '{}'", chainNodeInformation);

        synchronized (nodeMapping) {
            ConcurrentLinkedDeque<NodeUsage> usages = new ConcurrentLinkedDeque<>();
            ConcurrentLinkedDeque<NodeUsage> existingUsages = nodeUsages.putIfAbsent(chainNodeInformation, usages);
            if (existingUsages != null) {
                activate(chainNodeInformation);
                return -1;
            }
            int nodeID = nextNodeID.getAndIncrement();
            nodeMapping.put(nodeID, chainNodeInformation);
            return nodeID;
        }
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
