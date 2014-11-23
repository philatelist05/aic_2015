package at.ac.tuwien.aic.ws14.group2.onion.node.chain.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.Cell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.CellWorker;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.CellWorkerFactory;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Circuit;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.PrivateKey;


public class ChainCellWorkerFactory implements CellWorkerFactory {
    static final Logger logger = LogManager.getLogger(ChainCellWorkerFactory.class.getName());

    private final PrivateKey privateKey;

    public ChainCellWorkerFactory(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public CellWorker createCellWorker(ConnectionWorker connectionWorker, Cell cell, Circuit circuit) {
        return new ChainCellWorker(connectionWorker, cell, circuit, privateKey);
    }
}
