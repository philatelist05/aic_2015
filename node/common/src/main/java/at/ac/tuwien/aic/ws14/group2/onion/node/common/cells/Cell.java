package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 09.11.2014.
 */
public abstract class Cell {
    private short circuitID;
    private byte cellType;

    static final int CELL_BYTES = 512;
    static final int CELL_HEADER_BYTES = 3;   // sizeof(circuitID) + sizeof(cellType)
    static final int CELL_PAYLOAD_BYTES = CELL_BYTES - CELL_HEADER_BYTES;

    static final byte CELL_TYPE_CREATE = 0;
    static final byte CELL_TYPE_CREATE_RESPONSE = 1;
    static final byte CELL_TYPE_DESTROY = 2;
    static final byte CELL_TYPE_RELAY = 3;

    protected Cell() {
        // used when receive() is called
    }

    protected Cell(byte cellType, short circuitID) {
        this.cellType = cellType;
        this.circuitID = circuitID;
    }

    public static Cell receive(InputStream source) throws IOException, DecodeException {
        DataInputStream input = new DataInputStream(source);

        byte[] packet = new byte[CELL_BYTES];
        input.readFully(packet);

        ByteBuffer buffer = ByteBuffer.wrap(packet);

        short circuitID = buffer.getShort();
        byte cellType = buffer.get();

        Cell cell;
        switch (cellType) {
            case CELL_TYPE_CREATE:
                cell = new CreateCell(buffer);
                break;
            case CELL_TYPE_CREATE_RESPONSE:
                cell = new CreateResponseCell(buffer);
                break;
            case CELL_TYPE_DESTROY:
                cell = new DestroyCell();
                break;
            case CELL_TYPE_RELAY:
                cell = new RelayCell(buffer);
                break;
            default:
                throw new DecodeException();
        }

        cell.circuitID = circuitID;
        cell.cellType = cellType;

        return cell;
    }

    public void send(OutputStream destination) throws IOException {
        byte[] packet = new byte[CELL_BYTES];
        ByteBuffer buffer = ByteBuffer.wrap(packet);

        buffer.putShort(circuitID);
        buffer.put(cellType);
        encodePayload(buffer);

        destination.write(packet);
    }

    /**
     * Encodes the Cell payload into the specified buffer.
     */
    protected abstract void encodePayload(ByteBuffer buffer);

    public short getCircuitID() {
        return circuitID;
    }

    @Override
    public String toString() {
        return "Cell{" +
                "circuitID=" + circuitID +
                ", cellType=" + cellType +
                '}';
    }
}
