package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import java.nio.ByteBuffer;

/**
 * Created by Thomas on 09.11.2014.
 */
public class ExtendResponseCommand extends Command {

    private byte[] diffieHalf;
    private byte[] signature;

    /**
     * Reads a Command assuming that the Command Type field has already been read.
     * The Command type will not be set.
     */
    ExtendResponseCommand(ByteBuffer buffer) {
        diffieHalf = new byte[Cell.DIFFIE_HELLMAN_HALF_BYTES];
        signature = new byte[Cell.SIGNATURE_BYTES];

        buffer.get(diffieHalf);
        buffer.get(signature);
    }

    public ExtendResponseCommand(byte[] diffieHellmanHalf, byte[] signature) {
        super(COMMAND_TYPE_EXTEND_RESPONSE);

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
