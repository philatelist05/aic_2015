package at.ac.tuwien.aic.ws14.group2.onion.node.common;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 09.11.2014.
 */
public class CreateCell extends Cell {
    private byte[] encryptedDiffieHalf;

    /**
     * Reads and decodes the payload of a Create Cell assuming that the cell header has already been read.
     * Cell type and circuit ID will not be set.
     */
    CreateCell(ByteBuffer source) throws IOException {
        // TODO: read correct number of bytes
        encryptedDiffieHalf = new byte[Cell.DIFFIE_HELLMAN_HALF_BYTES];
        source.get(encryptedDiffieHalf);
    }

    public CreateCell(short circuitID, byte[] encryptedDiffieHellmanHalf) {
        super(Cell.CELL_TYPE_CREATE, circuitID);

        this.encryptedDiffieHalf = encryptedDiffieHellmanHalf;
    }

    public byte[] getDiffieHellmanHalf(byte[] privateKey) {
        // TODO: decrypt
        return encryptedDiffieHalf;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        buffer.put(encryptedDiffieHalf);
    }
}