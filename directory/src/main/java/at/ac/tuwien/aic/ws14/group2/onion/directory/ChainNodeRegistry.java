package at.ac.tuwien.aic.ws14.group2.onion.directory;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsageSummary;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;


public class ChainNodeRegistry {
    static final Logger logger = LogManager.getLogger(ChainNodeRegistry.class.getName());

    private final ConcurrentHashMap<Integer, ConcurrentLinkedDeque<NodeUsage>> nodeUsages;
    private final ConcurrentSkipListSet<ChainNodeInformation> activeNodes;
    private final ConcurrentSkipListSet<ChainNodeInformation> inactiveNodes;
    private final BiMap<Integer, ChainNodeInformation> nodeMapping;
    private final AtomicInteger nextNodeID;

    public ChainNodeRegistry() {
        logger.info("Initializing ChainNodeRegistry");
        this.activeNodes = new ConcurrentSkipListSet<>();
        this.inactiveNodes = new ConcurrentSkipListSet<>();
        this.nodeUsages = new ConcurrentHashMap<>();
        this.nodeMapping = Maps.synchronizedBiMap(HashBiMap.create());
        this.nextNodeID = new AtomicInteger();
    }

    //TODO check signature?
    public boolean addNodeUsage(int chainNodeID, NodeUsage usage) {
        logger.debug("Recording NodeUsage for ChainNode '{}': {}", chainNodeID, usage);

        ConcurrentLinkedDeque<NodeUsage> usages = nodeUsages.get(chainNodeID);

        if (usages == null) {
            //TODO Exception
            logger.warn("Cannot record NodeUsage for unknown ID '{}'", chainNodeID);
            return false;
        } else {
            usages.addLast(usage);
            ChainNodeInformation chainNodeInformation = nodeMapping.get(chainNodeID);
            if (!activeNodes.contains(chainNodeInformation)) {
                activate(chainNodeInformation);
            }
            return true;
        }
    }

    public int addNewChainNode(ChainNodeInformation chainNodeInformation) {
        logger.info("Adding new ChainNode '{}'", chainNodeInformation);

        if (nodeMapping.containsValue(chainNodeInformation)) {
            activate(chainNodeInformation);
            return -1;
        }
        int nodeID = nextNodeID.getAndIncrement();
        nodeMapping.put(nodeID, chainNodeInformation);
        nodeUsages.put(nodeID, new ConcurrentLinkedDeque<>());
        return nodeID;
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
        int nodeID = nodeMapping.inverse().get(nodeInformation);
        ConcurrentLinkedDeque<NodeUsage> usages = nodeUsages.get(nodeID);
        return usages == null ? null : usages.getLast();
    }
}
