package at.ac.tuwien.aic.ws14.group2.onion.directory.handler;

import at.ac.tuwien.aic.ws14.group2.onion.directory.ChainNodeRegistry;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.DirectoryService;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;
import at.ac.tuwien.aic.ws14.group2.onion.directory.exceptions.NoSuchChainNodeAvailable;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TException;

import java.util.*;

public class ServiceImplementation implements DirectoryService.Iface {
    static final Logger logger = LogManager.getLogger(ServiceImplementation.class.getName());

    private ChainNodeRegistry chainNodeRegistry;
    private Random random = new Random();

    public ServiceImplementation(ChainNodeRegistry chainNodeRegistry) {
        this.chainNodeRegistry = chainNodeRegistry;
    }

    // determines how the chain should be selected (used only for debugging)
    int chainSelection;
    boolean debugging = false;

    @Override
    public boolean heartbeat(int nodeID, NodeUsage nodeUsage) throws TException {
        try {
            chainNodeRegistry.addNodeUsage(nodeID, nodeUsage);
            return true;
        } catch (NoSuchChainNodeAvailable e) {
            logger.warn("Received heartbeat for not registered chainnode..");
            logger.catching(Level.DEBUG, e);
            return false;
        }
    }

    @Override
    public int registerNode(ChainNodeInformation nodeInformation) throws TException {
        return chainNodeRegistry.addNewChainNode(nodeInformation);
    }

    @Override
    public List<ChainNodeInformation> getChain(int chainLength) throws TException {
        if (chainLength < 1) {
            logger.error("requested chain too short");
            return null;
        }

        ArrayList<NodeScore> scores = new ArrayList<>();

        // calculate a score for each node representing the probability to be chosen
        Set<Integer> ids = chainNodeRegistry.getActiveNodeIDs();
        for (Integer id : ids) {
            NodeScore score = new NodeScore();

            score.setNodeID(id);

            // NodeUsage might have been deleted in the meantime!
            NodeUsage nodeUsage = chainNodeRegistry.getLastNodeUsage(id);
            if (nodeUsage == null)
                continue;

            // ChainNodeInformation might have been deleted in the meantime!
            score.setNodeInfo(chainNodeRegistry.getChainNodeInformation(id));
            if (score.getNodeInfo() == null)
                continue;

            score.setChainNodeScore(calculateChainNodeScore(nodeUsage));
            score.setTargetNodeScore(calculateTargetNodeScore(nodeUsage));

            scores.add(score);
        }

        if (scores.size() < chainLength) {
            logger.error("not enough active nodes ({}) for requested chain length", scores.size());
            return null;
        }

        if (debugging) {
            if (chainSelection == 0) {
                scores.sort((a, b) -> Integer.compare(a.getNodeID(), b.getNodeID()));
                chainSelection = 1;
            } else {
                scores.sort((a, b) -> Integer.compare(b.getNodeID(), a.getNodeID()));
                chainSelection = 0;
            }

            ArrayList<ChainNodeInformation> selectedNodes = new ArrayList<>();
            for (int i = 0; i < chainLength; i++) {
                selectedNodes.add(scores.get(i).getNodeInfo());
            }

            return selectedNodes;
        }

        // select target node
        NodeScore targetScore = Collections.max(scores, (a, b) -> Double.compare(a.getTargetNodeScore(), b.getTargetNodeScore()));
        scores.remove(targetScore);

        ArrayList<ChainNodeInformation> selectedNodes = new ArrayList<>();

        // select other chain nodes
        scores.sort((a, b) -> Double.compare(b.getChainNodeScore(), a.getChainNodeScore()));
        for (int i = 0; i < chainLength - 1; i++) {
            selectedNodes.add(scores.get(i).getNodeInfo());
        }

        selectedNodes.add(targetScore.getNodeInfo());

        return selectedNodes;
    }

    /**
     * Returns the score/probability for a node to become a chain node but no target node.
     */
    private double calculateChainNodeScore(NodeUsage nodeUsage) {
        return calculateProbability(nodeUsage);
    }

    /**
     * Returns the score/probability for a node to become a target node.
     */
    private double calculateTargetNodeScore(NodeUsage nodeUsage) {
        return calculateProbability(nodeUsage);
    }

    private double calculateProbability(NodeUsage nodeUsage) {
        final double CHAIN_COUNT_WEIGHT = 0.4;
        final double P = 0.1;
        final double M = 1000;

        // value1 == 1 if nodeUsage.getChainCount() == 0
        // value1 == P if nodeUsage.getChainCount() == M
        double value1 = 1 / (1 + (1 - P) / (P * M) * nodeUsage.getChainCount());
        double value2 = random.nextDouble();

        return value1 * CHAIN_COUNT_WEIGHT + value2 * (1 - CHAIN_COUNT_WEIGHT);
    }
}
