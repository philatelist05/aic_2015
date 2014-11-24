package at.ac.tuwien.aic.ws14.group2.onion.node.local.node;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChainMetaData {
    static final Logger logger = LogManager.getLogger(ChainNodeMetaData.class.getName());

    private ConcurrentHashMap<Integer, ChainNodeMetaData> nodes;
    private Integer lastNode;

    public ChainMetaData(ConcurrentHashMap<Integer, ChainNodeMetaData> nodes) {
        this.nodes = nodes;
        this.lastNode = -1;
    }

    public static ChainMetaData fromChainNodeInformationList(List<ChainNodeInformation> nodeInformationList) {
        ConcurrentHashMap<Integer, ChainNodeMetaData> nodes = new ConcurrentHashMap<>();
        for (int i = 0; i < nodeInformationList.size(); i++) {
            ChainNodeMetaData nodeMetaData = ChainNodeMetaData.fromChainNodeInformation(nodeInformationList.get(i));
            if (nodeMetaData == null) {
                logger.warn("Could not convert List with ChainNodeInformations to ChainMetaData, returning null.");
                return null; //TODO FG throw exception instead?
            } else {
                nodes.putIfAbsent(i, nodeMetaData);
            }
        }
        return new ChainMetaData(nodes);
    }

    public ConcurrentHashMap<Integer, ChainNodeMetaData> getNodes() {
        return nodes;
    }

    public int getLastNode() {
        return lastNode;
    }

    public boolean growChain(byte[] sessionKey) {
        synchronized (lastNode) {
            if (lastNode < nodes.size()) {
                lastNode++;
                ChainNodeMetaData nodeMetaData = nodes.get(lastNode);
                nodeMetaData.setSessionKey(sessionKey);
                nodes.replace(lastNode, nodeMetaData);
                return true;
            } else {
                logger.warn("No more nodes left to grow chain, returning false.");
                return false;
            }
        }
    }
}
