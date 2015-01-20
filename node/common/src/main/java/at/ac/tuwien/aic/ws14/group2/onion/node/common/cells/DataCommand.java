package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 10.11.2014.
 */
public class DataCommand extends Command {

    public static final int MAX_DATA_LENGTH = COMMAND_PAYLOAD_BYTES - 2 /* length */ - 4 /* sequenceNumber */;

    private final byte[] data;
    private final short length;
    private final long sequenceNumber;

    /**
     * Reads a Command assuming that the Command Type field has already been read.
     * The Command type will not be set.
     */
    DataCommand(ByteBuffer buffer) {
        sequenceNumber = Integer.toUnsignedLong(buffer.getInt());
        length = buffer.getShort();

        data  = new byte[length];
        buffer.get(data);
    }

    /**
     * Encapsulates the next bytes of a stream in a Data Command.
     * @throws at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException Thrown if end of stream is reached.
     */
    public DataCommand(long sequenceNumber, InputStream source) throws IOException, DecodeException {
        super(COMMAND_TYPE_DATA);

        data = new byte[MAX_DATA_LENGTH];
        length = (short)source.read(data);

        if (length <= 0)
            throw new DecodeException("No data");

        if (sequenceNumber > Integer.toUnsignedLong(-1))
            throw new IllegalArgumentException("Sequence number must be below 0xFFFFFFFF");
        this.sequenceNumber = sequenceNumber;
    }

    public DataCommand(long sequenceNumber, byte[] data) throws DecodeException {
        super(COMMAND_TYPE_DATA);

        if (data.length <= 0)
            throw new DecodeException("No data");
        if (data.length > MAX_DATA_LENGTH)
            throw new DecodeException("Too much data for a single DataCommand.");

        this.data = data;
        this.length = (short)data.length;

        if (sequenceNumber > Integer.toUnsignedLong(-1))
            throw new IllegalArgumentException("Sequence number must be below 0xFFFFFFFF");
        this.sequenceNumber = sequenceNumber;
    }

    public void sendData(OutputStream destination) throws IOException {
        destination.write(data, 0, length);
    }

    public byte[] getData() {
        return data;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        buffer.putInt((int) sequenceNumber);
        buffer.putShort(length);
        buffer.put(data, 0, length);
    }

    @Override
    public String toString() {
        return "DataCommand{" +
                "length=" + length +
                ", sequenceNumber=" + sequenceNumber +
                '}';
    }
}
