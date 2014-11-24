package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;

import java.net.*;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 09.11.2014.
 */
public class ExtendCommand extends Command {

    private Endpoint endpoint;
    private byte[] encryptedDiffieHalf;

    /**
     * Reads a Command assuming that the Command Type field has already been read.
     * The Command type will not be set.
     */
    ExtendCommand(ByteBuffer buffer) throws DecodeException {
        endpoint = new Endpoint(buffer);

        encryptedDiffieHalf = new byte[Cell.DIFFIE_HELLMAN_HALF_BYTES];
        buffer.get(encryptedDiffieHalf);
    }

    public ExtendCommand(Endpoint endpoint, byte[] encryptedDiffieHalf) {
        super(COMMAND_TYPE_EXTEND);

        this.endpoint = endpoint;
        this.encryptedDiffieHalf = encryptedDiffieHalf;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public byte[] getDiffieHellmanHalf(byte[] privateKey) {
        // TODO: decrypt
        return encryptedDiffieHalf;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        endpoint.encode(buffer);
        buffer.put(encryptedDiffieHalf);
    }
}
