package at.ac.tuwien.aic.ws14.group2.onion.node.local.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.Cell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.CellWorker;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.CellWorkerFactory;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Circuit;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorker;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.SocksCallBack;

import java.security.PrivateKey;
import java.util.concurrent.ConcurrentHashMap;

public class LocalCellWorkerFactory implements CellWorkerFactory {

    private LocalNodeCore nodeCore;

    public LocalCellWorkerFactory(LocalNodeCore nodeCore) {
        this.nodeCore = nodeCore;
    }

    @Override
    public CellWorker createCellWorker(ConnectionWorker connectionWorker, Cell cell, Circuit circuit) {
        return new LocalCellWorker(connectionWorker, cell, circuit, nodeCore);
    }
}
