package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 09.11.2014.
 */
public class CreateResponseCell extends Cell {
    private byte[] diffieHalf;
    private byte[] signature;

    /**
     * Reads and decodes the payload of a Create Response Cell assuming that the cell header has already been read.
     * Cell type and circuit ID will not be set.
     */
    CreateResponseCell(ByteBuffer source) throws IOException {
        // TODO: read correct number of bytes

        diffieHalf = new byte[DIFFIE_HELLMAN_HALF_BYTES];
        signature = new byte[SIGNATURE_BYTES];

        source.get(diffieHalf);
        source.get(signature);
    }

    public CreateResponseCell(short circuitID, byte[] diffieHellmanHalf, byte[] signature) {
        super(CELL_TYPE_CREATE_RESPONSE, circuitID);

        this.diffieHalf = diffieHellmanHalf;
        this.signature = signature;
    }

    public byte[] getDiffieHellmanHalf() {
        return diffieHalf;
    }

    public byte[] getSignature() {
        return signature;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        buffer.put(diffieHalf);
        buffer.put(signature);
    }
}