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
    private EncryptedDHHalf encryptedDHHalf;

    /**
     * Reads a Command assuming that the Command Type field has already been read.
     * The Command type will not be set.
     */
    ExtendCommand(ByteBuffer buffer) throws DecodeException {
        endpoint = new Endpoint(buffer);
        encryptedDHHalf = new EncryptedDHHalf(buffer);
    }

    public ExtendCommand(Endpoint endpoint, BigInteger prime1, BigInteger prime2, EncryptedDHHalf encryptedDHHalf) {
        super(COMMAND_TYPE_EXTEND);

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
