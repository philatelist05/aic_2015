package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 10.11.2014.
 */
public class DataCommand extends Command {

    public static final int MAX_DATA_LENGTH = COMMAND_PAYLOAD_BYTES - 4;

    private byte[] data;
    private short length;
    private short sequenceNumber;

    /**
     * Reads a Command assuming that the Command Type field has already been read.
     * The Command type will not be set.
     */
    DataCommand(ByteBuffer buffer) {
        sequenceNumber = buffer.getShort();
        length = buffer.getShort();

        data  = new byte[length];
        buffer.get(data);
    }

    /**
     * Encapsulates the next bytes of a stream in a Data Command.
     * @throws at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException Thrown if end of stream is reached.
     */
    public DataCommand(InputStream source) throws IOException, DecodeException {
        super(COMMAND_TYPE_DATA);

        data = new byte[MAX_DATA_LENGTH];
        length = (short)source.read(data);

        if (length <= 0)
            throw new DecodeException();
    }

    public DataCommand(byte[] data) throws DecodeException {
        super(COMMAND_TYPE_DATA);

        if (data.length > MAX_DATA_LENGTH)
            throw new DecodeException("Too much data for a single DataCommand.");

        this.data = data;
        this.length = (short)data.length;
    }

    public void sendData(OutputStream destination) throws IOException {
        destination.write(data, 0, length);
    }

    public byte[] getData() {
        return data;
    }

    public Short getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Short sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        buffer.putShort(sequenceNumber);
        buffer.putShort(length);
        buffer.put(data, 0, length);
    }
}
