package at.ac.tuwien.aic.ws14.group2.onion.node.common;

import java.nio.ByteBuffer;

/**
 * Created by Thomas on 09.11.2014.
 */
public class Command {


    static final byte COMMAND_EXTEND = 0;
    static final byte COMMAND_EXTEND_RESPONSE = 1;
//    static final byte COMMAND_CONNECT = 2;
//    static final byte COMMAND_CONNECT_RESPONSE = 3;
//    static final byte COMMAND_CLOSE = 4;
//    static final byte COMMAND_CLOSE_RESPONSE = 5;

    /**
     * Decodes a command packet.
     * @param packet A command packet of size Cell.CELL_PAYLOAD_BYTES
     */
    public static Command decode(byte[] packet) throws DecodeException {
        ByteBuffer buffer = ByteBuffer.wrap(packet);

        byte command = buffer.get();
        switch (command) {
            case COMMAND_EXTEND:
                return new ExtendCommand(buffer);
            case COMMAND_EXTEND_RESPONSE:
                return new ExtendResponseCommand(buffer);
            default:
                throw new DecodeException();
        }
    }
}
