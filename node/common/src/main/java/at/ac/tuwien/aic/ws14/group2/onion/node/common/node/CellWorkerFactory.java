package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.Cell;

/**
 * Created by Thomas on 22.11.2014.
 */
public interface CellWorkerFactory {

    /**
     * Creates a new CellWorker which is going to process the specified cell.
     * @param cell A cell to be processed by the created worker.
     * @param circuit The circuit object for the circuit ID that is encoded in the given cell.
     * @return
     */
    CellWorker createCellWorker(ConnectionWorker connectionWorker, Cell cell, Circuit circuit);
}
