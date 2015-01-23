package at.ac.tuwien.aic.ws14.group2.onion.directory.handler;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;

/**
 * Created by Thomas on 22.01.2015.
 */
public class NodeScore {
    private int nodeID;
    private ChainNodeInformation nodeInfo;
    private double chainNodeScore;    // the score for becoming a chain node (the higher the better)
    private double targetNodeScore;   // the score for becoming a target node (the higher the better)

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public ChainNodeInformation getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(ChainNodeInformation nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public double getChainNodeScore() {
        return chainNodeScore;
    }

    public void setChainNodeScore(double chainNodeScore) {
        this.chainNodeScore = chainNodeScore;
    }

    public double getTargetNodeScore() {
        return targetNodeScore;
    }

    public void setTargetNodeScore(double targetNodeScore) {
        this.targetNodeScore = targetNodeScore;
    }
}
