package at.ac.tuwien.aic.ws14.group2.onion.node.common;

import java.io.DataInputStream;
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
     */
    public CreateResponseCell(ByteBuffer source) throws IOException {
        // TODO: read correct number of bytes

        diffieHalf = new byte[Cell.DIFFIE_HELLMAN_HALF_BYTES];
        signature = new byte[Cell.SIGNATURE_BYTES];

        source.get(diffieHalf);
        source.get(signature);
    }

    public byte[] getDiffieHellmanHalf() {
        return diffieHalf;
    }

    public byte[] getSignature() {
        return signature;
    }
}
