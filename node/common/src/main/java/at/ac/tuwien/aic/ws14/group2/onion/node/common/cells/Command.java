package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import java.nio.ByteBuffer;

/**
 * Created by Thomas on 09.11.2014.
 */
public abstract class Command {
    private byte type;

    static final int COMMAND_BYTES = Cell.CELL_PAYLOAD_BYTES;
    static final int COMMAND_HEADER_BYTES = 1;   // sizeof(type)
    static final int COMMAND_PAYLOAD_BYTES = COMMAND_BYTES - COMMAND_HEADER_BYTES;

    static final byte COMMAND_TYPE_EXTEND = 0;
    static final byte COMMAND_TYPE_EXTEND_RESPONSE = 1;
    static final byte COMMAND_TYPE_CONNECT = 2;
    static final byte COMMAND_TYPE_CONNECT_RESPONSE = 3;
    static final byte COMMAND_TYPE_DATA = 4;

    protected Command() {
        // used when decode() is called
    }

    protected Command(byte type) {
        this.type = type;
    }

    /**
     * Decodes a type packet.
     * @param packet A type packet of size Cell.CELL_PAYLOAD_BYTES
     */
    public static Command decode(byte[] packet) throws DecodeException {
        ByteBuffer buffer = ByteBuffer.wrap(packet);

        byte cmdValue = buffer.get();

        Command cmd;
        switch (cmdValue) {
            case COMMAND_TYPE_EXTEND:
                cmd = new ExtendCommand(buffer);
                break;
            case COMMAND_TYPE_EXTEND_RESPONSE:
                cmd = new ExtendResponseCommand(buffer);
                break;
            case COMMAND_TYPE_CONNECT:
                cmd = new ConnectCommand(buffer);
                break;
            case COMMAND_TYPE_CONNECT_RESPONSE:
                cmd = new ConnectResponseCommand();
                break;
            case COMMAND_TYPE_DATA:
                cmd = new DataCommand(buffer);
                break;
            default:
                throw new DecodeException();
        }

        cmd.type = cmdValue;

        return cmd;
    }

    /**
     * Encodes this Command.
     * @return A new byte array of size Cell.CELL_PAYLOAD_BYTES.
     *         Changes in this object are not reflected in the returned array.
     */
    public byte[] encode() {
        byte[] command = new byte[COMMAND_BYTES];
        ByteBuffer buffer = ByteBuffer.wrap(command);

        buffer.put(type);
        encodePayload(buffer);

        return command;
    }

    /**
     * Encodes the Command payload into the specified buffer.
     */
    protected abstract void encodePayload(ByteBuffer buffer);
}
