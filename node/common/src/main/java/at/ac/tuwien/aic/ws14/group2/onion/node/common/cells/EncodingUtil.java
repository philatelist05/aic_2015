package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import java.nio.ByteBuffer;

/**
 * Created by Thomas on 29.11.2014.
 */
public class EncodingUtil {

    /**
     * Reads a byte array with length field.
     */
    public static byte[] readByteArray(ByteBuffer input) {
        short length = input.getShort();
        byte[] buffer = new byte[length];

        input.get(buffer);

        return buffer;
    }

    /**
     * Writes a byte array with length field.
     */
    public static void writeByteArray(byte[] data, ByteBuffer output) {
        output.putShort((short)data.length);
        output.put(data);
    }
}
