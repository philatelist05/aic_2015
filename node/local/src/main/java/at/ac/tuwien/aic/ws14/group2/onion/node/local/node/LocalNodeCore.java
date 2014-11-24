package at.ac.tuwien.aic.ws14.group2.onion.node.local.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.Cell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Circuit;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorker;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorkerFactory;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.SocksCallBack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class LocalNodeCore {
    static final Logger logger = LogManager.getLogger(LocalNodeCore.class.getName());

    private ConcurrentHashMap<Short, ChainMetaData> chains;
    private ConcurrentHashMap<Short, SocksCallBack> callbacks;

    public LocalNodeCore(ConcurrentHashMap<Short, ChainMetaData> chains, ConcurrentHashMap<Short, SocksCallBack> callbacks) {
        this.chains = chains;
        this.callbacks = callbacks;
    }

    public LocalNodeCore() {
        this.chains = new ConcurrentHashMap<>();
        this.callbacks = new ConcurrentHashMap<>();
    }

    public ChainMetaData getChainMetaData(Short circuitID) {
        return chains.get(circuitID);
    }

    public SocksCallBack getCallBack(Short circuitID) {
        return callbacks.get(circuitID);
    }

    public void createChain(ChainMetaData chainMetaData, SocksCallBack callBack) {
        /* TODO spawn Thread to start Chainbuilding
           TODO use a new protected method to register circuit once first Create/CreateResponse cycle is complete?
         */
    }

    public void sendCell(Cell cell, Circuit circuit, SocksCallBack callBack) {
        // TODO use a ThreadPool for this for performance?
        try {
            ConnectionWorker connectionWorker = ConnectionWorkerFactory.getInstance().getConnectionWorker(circuit.getEndpoint());
            connectionWorker.sendCell(cell);
        } catch (IOException e) {
            logger.warn("Encountered IOException while trying to send cell to Circuit {}: {}", circuit, e.getMessage());
            return; //TODO use callBack instead to communicate error
        }
    }

    public boolean removeChain(Short circuitID) {
        boolean success = true;
        if(chains.remove(circuitID) == null) {
            success = false;
        }
        if(callbacks.remove(circuitID) == null) {
            success = false;
        }
        return success;
    }
}
