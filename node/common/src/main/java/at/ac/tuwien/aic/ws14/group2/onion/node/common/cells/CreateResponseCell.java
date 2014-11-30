package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 09.11.2014.
 */
public class CreateResponseCell extends Cell {
    private byte[] dhPublicKey;
    private byte[] signature;

    /**
     * Reads and decodes the payload of a Create Response Cell assuming that the cell header has already been read.
     * Cell type and circuit ID will not be set.
     */
    CreateResponseCell(ByteBuffer source) throws IOException {
        dhPublicKey = EncodingUtil.readByteArray(source);
        signature = EncodingUtil.readByteArray(source);
    }

    public CreateResponseCell(short circuitID, byte[] dhPublicKey, byte[] signature) {
        super(CELL_TYPE_CREATE_RESPONSE, circuitID);

        this.dhPublicKey = dhPublicKey;
        this.signature = signature;
    }

    public byte[] getDhPublicKey() {
        return dhPublicKey;
    }

    public byte[] getSignature() {
        return signature;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        EncodingUtil.writeByteArray(dhPublicKey, buffer);
        EncodingUtil.writeByteArray(signature, buffer);
    }
}
