package at.ac.tuwien.aic.ws14.group2.onion.node.common;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 10.11.2014.
 */
public class DataCommand extends Command {

    static final int MAX_DATA_LENGTH = Command.COMMAND_PAYLOAD_BYTES - 2;

    private byte[] data;
    private short length;

    /**
     * Reads a Command assuming that the Command Type field has already been read.
     * The Command type will not be set.
     */
    DataCommand(ByteBuffer buffer) {
        length = buffer.getShort();

        data  = new byte[length];
        buffer.get(data);
    }

    /**
     * Encapsulates the next bytes of a stream in a Data Command.
     * @throws at.ac.tuwien.aic.ws14.group2.onion.node.common.DecodeException Thrown if end of stream is reached.
     */
    public DataCommand(InputStream source) throws IOException, DecodeException {
        super(COMMAND_TYPE_DATA);

        data = new byte[MAX_DATA_LENGTH];
        length = (short)source.read(data);

        if (length <= 0)
            throw new DecodeException();
    }

    public void sendData(OutputStream destination) throws IOException {
        destination.write(data, 0, length);
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        buffer.putShort(length);
        buffer.put(data, 0, length);
    }
}
