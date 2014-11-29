package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 09.11.2014.
 */
public class CreateResponseCell extends Cell {
    private DHHalf dhHalf;
    private byte[] signature;

    /**
     * Reads and decodes the payload of a Create Response Cell assuming that the cell header has already been read.
     * Cell type and circuit ID will not be set.
     */
    CreateResponseCell(ByteBuffer source) throws IOException {
        dhHalf = new DHHalf(source);
        signature = EncodingUtil.readByteArray(source);
    }

    public CreateResponseCell(short circuitID, DHHalf dhHalf, byte[] signature) {
        super(CELL_TYPE_CREATE_RESPONSE, circuitID);

        this.dhHalf = dhHalf;
        this.signature = signature;
    }

    public DHHalf getDHHalf() {
        return dhHalf;
    }

    public byte[] getSignature() {
        return signature;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        dhHalf.encode(buffer);
        EncodingUtil.writeByteArray(signature, buffer);
    }
}
