package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 09.11.2014.
 */
public class RelayCell extends Cell {
    private RelayCellPayload payload;

    /**
     * Reads and decodes the payload of a Relay Cell assuming that the cell header has already been read.
     * Cell type and circuit ID will not be set.
     */
    RelayCell(ByteBuffer source) throws IOException {
        byte[] data = new byte[CELL_PAYLOAD_BYTES];
        source.get(data);

        payload = new RelayCellPayload(data);
    }

    public RelayCell(short circuitID, RelayCellPayload payload) {
        super(CELL_TYPE_RELAY, circuitID);

        this.payload = payload;
    }

    public RelayCellPayload getPayload() {
        return payload;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        buffer.put(payload.encode());
    }
}
