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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ServiceImplementation implements DirectoryService.Iface {
    static final Logger logger = LogManager.getLogger(ServiceImplementation.class.getName());

    private ChainNodeRegistry chainNodeRegistry;

    public ServiceImplementation(ChainNodeRegistry chainNodeRegistry) {
        this.chainNodeRegistry = chainNodeRegistry;
    }


    @Override
    public boolean heartbeat(int nodeID, NodeUsage nodeUsage) throws TException {
        try {
            chainNodeRegistry.addNodeUsage(nodeID, nodeUsage);
            return true;
        } catch (NoSuchChainNodeAvailable e) {
            logger.catching(Level.WARN, e);
            return false;
        }
    }

    @Override
    public int registerNode(ChainNodeInformation nodeInformation) throws TException {
        return chainNodeRegistry.addNewChainNode(nodeInformation);
    }

    @Override
    public List<ChainNodeInformation> getChain(int chainLength) throws TException {
        Set<ChainNodeInformation> activeNodes = chainNodeRegistry.getActiveNodes();
        if (activeNodes.size() < chainLength) {
            return null;
        } else {
            ArrayList<ChainNodeInformation> chainNodes = new ArrayList<>(chainLength);
            Iterator<ChainNodeInformation> iterator = activeNodes.iterator();
            while (chainNodes.size() < chainLength) {
                chainNodes.add(iterator.next());
            }
            return chainNodes;
        }
    }
}
