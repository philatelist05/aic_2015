package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.DHKeyExchange;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.CellException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;

/**
 * Created by Thomas on 09.11.2014.
 */
public class CreateCell extends Cell {
    private byte[] encryptedDiffieHalf;
    private Endpoint endpoint;


    /**
     * Reads and decodes the payload of a Create Cell assuming that the cell header has already been read.
     * Cell type and circuit ID will not be set.
     */
    CreateCell(ByteBuffer source) throws DecodeException {
        // TODO: read correct number of bytes
        encryptedDiffieHalf = new byte[DIFFIE_HELLMAN_HALF_BYTES];
        source.get(encryptedDiffieHalf);

        endpoint = new Endpoint(source);
    }

    public CreateCell(short circuitID, byte[] encryptedDiffieHellmanHalf, Endpoint endpoint) {
        super(CELL_TYPE_CREATE, circuitID);
        this.endpoint = endpoint;
        this.encryptedDiffieHalf = encryptedDiffieHellmanHalf;
    }

    public byte[] getDiffieHellmanHalf(PrivateKey privateKey) {
        // TODO: decrypt
        return encryptedDiffieHalf;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        buffer.put(encryptedDiffieHalf);
        endpoint.encode(buffer);
    }
}
