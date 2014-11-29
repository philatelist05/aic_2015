package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;

import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 09.11.2014.
 */
public class ExtendCommand extends Command {
    private Endpoint endpoint;
    private BigInteger prime1;
    private BigInteger prime2;
    private EncryptedDHHalf encryptedDHHalf;

    /**
     * Reads a Command assuming that the Command Type field has already been read.
     * The Command type will not be set.
     */
    ExtendCommand(ByteBuffer buffer) throws DecodeException {
        endpoint = new Endpoint(buffer);
        prime1 = new BigInteger(EncodingUtil.readByteArray(buffer));
        prime2 = new BigInteger(EncodingUtil.readByteArray(buffer));
        encryptedDHHalf = new EncryptedDHHalf(buffer);
    }

    public ExtendCommand(Endpoint endpoint, BigInteger prime1, BigInteger prime2, EncryptedDHHalf encryptedDHHalf) {
        super(COMMAND_TYPE_EXTEND);

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
