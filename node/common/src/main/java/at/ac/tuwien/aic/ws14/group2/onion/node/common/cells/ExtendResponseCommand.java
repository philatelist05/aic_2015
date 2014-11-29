package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import java.nio.ByteBuffer;

/**
 * Created by Thomas on 09.11.2014.
 */
public class ExtendResponseCommand extends Command {
    private DHHalf dhHalf;
    private byte[] signature;

    /**
     * Reads a Command assuming that the Command Type field has already been read.
     * The Command type will not be set.
     */
    ExtendResponseCommand(ByteBuffer buffer) {
        dhHalf = new DHHalf(buffer);
        signature = EncodingUtil.readByteArray(buffer);
    }

    public ExtendResponseCommand(DHHalf dhHalf, byte[] signature) {
        super(COMMAND_TYPE_EXTEND_RESPONSE);

        this.dhHalf = dhHalf;
        this.signature = signature;
    }

    public DHHalf getDHHalf() {
        return dhHalf;
    }

    public byte[] getSignature() {
        return signature;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        dhHalf.encode(buffer);
        EncodingUtil.writeByteArray(signature, buffer);
    }
}
