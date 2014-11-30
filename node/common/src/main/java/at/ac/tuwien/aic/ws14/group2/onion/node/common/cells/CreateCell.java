package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.DHKeyExchange;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.CellException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by Thomas on 09.11.2014.
 */
public class CreateCell extends Cell {
    private Endpoint endpoint;
    private EncryptedDHHalf encryptedDHHalf;

    /**
     * Reads and decodes the payload of a Create Cell assuming that the cell header has already been read.
     * Cell type and circuit ID will not be set.
     */
    CreateCell(ByteBuffer source) throws DecodeException {
        endpoint = new Endpoint(source);
        encryptedDHHalf = new EncryptedDHHalf(source);
    }

    public CreateCell(short circuitID, Endpoint endpoint, EncryptedDHHalf encryptedDHHalf) {
        super(CELL_TYPE_CREATE, circuitID);

        this.endpoint = endpoint;
        this.encryptedDHHalf = encryptedDHHalf;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public EncryptedDHHalf getDHHalf() {
        return encryptedDHHalf;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        endpoint.encode(buffer);
        encryptedDHHalf.encode(buffer);
    }
}
