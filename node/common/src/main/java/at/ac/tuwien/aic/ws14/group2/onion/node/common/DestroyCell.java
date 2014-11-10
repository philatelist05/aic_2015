package at.ac.tuwien.aic.ws14.group2.onion.node.common;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 10.11.2014.
 */
public class DestroyCell extends Cell {
    /**
     * Creates a Destroy Cell without setting the Cell header.
     */
    public DestroyCell() {
    }

    public DestroyCell(short circuitID) {
        super(Cell.CELL_TYPE_DESTROY, circuitID);
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
    }
}
