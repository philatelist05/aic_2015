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
    private BigInteger prime1;
    private BigInteger prime2;
    private EncryptedDHHalf encryptedDHHalf;

    /**
     * Reads and decodes the payload of a Create Cell assuming that the cell header has already been read.
     * Cell type and circuit ID will not be set.
     */
    CreateCell(ByteBuffer source) throws DecodeException {
        endpoint = new Endpoint(source);
        prime1 = new BigInteger(EncodingUtil.readByteArray(source));
        prime2 = new BigInteger(EncodingUtil.readByteArray(source));
        encryptedDHHalf = new EncryptedDHHalf(source);
    }

    public CreateCell(short circuitID, Endpoint endpoint, BigInteger prime1, BigInteger prime2, EncryptedDHHalf encryptedDHHalf) {
        super(CELL_TYPE_CREATE, circuitID);

        this.endpoint = endpoint;
        this.prime1 = prime1;
        this.prime2 = prime2;
        this.encryptedDHHalf = encryptedDHHalf;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public BigInteger getPrime1() {
        return prime1;
    }

    public BigInteger getPrime2() {
        return prime2;
    }

    public EncryptedDHHalf getDHHalf() {
        return encryptedDHHalf;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        endpoint.encode(buffer);
        EncodingUtil.writeByteArray(prime1.toByteArray(), buffer);
        EncodingUtil.writeByteArray(prime2.toByteArray(), buffer);
        encryptedDHHalf.encode(buffer);
    }
}
