package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;

import java.nio.ByteBuffer;

/**
 * Created by Thomas on 09.11.2014.
 */
public class ExtendResponseCommand extends Command {
    private byte[] dhPublicKey;
    private byte[] signature;
    private CreateStatus status;

    /**
     * Reads a Command assuming that the Command Type field has already been read.
     * The Command type will not be set.
     */
    ExtendResponseCommand(ByteBuffer buffer) throws DecodeException {
        dhPublicKey = EncodingUtil.readByteArray(buffer);
        signature = EncodingUtil.readByteArray(buffer);
        status = CreateStatus.fromByte(buffer.get());
    }

    public ExtendResponseCommand(byte[] dhPublicKey, byte[] signature, CreateStatus status) {
        super(COMMAND_TYPE_EXTEND_RESPONSE);

        this.dhPublicKey = dhPublicKey;
        this.signature = signature;
        this.status = status;
    }

    public byte[] getDHPublicKey() {
        return dhPublicKey;
    }

    public byte[] getSignature() {
        return signature;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        EncodingUtil.writeByteArray(dhPublicKey, buffer);
        EncodingUtil.writeByteArray(signature, buffer);
        buffer.put(status.toByte());
    }
}
