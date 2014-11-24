package at.ac.tuwien.aic.ws14.group2.onion.node.local.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.Cell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.CellWorker;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Circuit;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorker;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.SocksCallBack;

import java.util.concurrent.ConcurrentHashMap;

public class LocalCellWorker implements CellWorker {
    private final ConnectionWorker connectionWorker;
    private final Cell cell;
    private final Circuit circuit;
    private final LocalNodeCore nodeCore;

    public LocalCellWorker(ConnectionWorker connectionWorker, Cell cell, Circuit circuit, LocalNodeCore nodeCore) {
        this.connectionWorker = connectionWorker;
        this.cell = cell;
        this.circuit = circuit;
        this.nodeCore = nodeCore;
    }

    @Override
    public void run() {
        //TODO implement
    }
}
