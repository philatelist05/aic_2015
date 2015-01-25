package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;

import java.nio.ByteBuffer;

/**
 * Created by Thomas on 25.01.2015.
 */
public class ErrorCell extends Cell {
    private Endpoint endpoint;
    private EncryptedDHHalf encryptedDHHalf;
    private byte errorCode;

    public static final byte ERROR_CODE_CONNECTION_WORKER_ALREADY_EXISTS = 1;

    /**
     * Reads and decodes the payload of an Error Cell assuming that the cell header has already been read.
     * Cell type and circuit ID will not be set.
     */
    ErrorCell(ByteBuffer source) throws DecodeException {
        endpoint = new Endpoint(source);
        encryptedDHHalf = new EncryptedDHHalf(source);
        errorCode = source.get();
    }

    public ErrorCell(short circuitID, Endpoint endpoint, EncryptedDHHalf encryptedDHHalf, byte errorCode) {
        super(CELL_TYPE_ERROR, circuitID);

        this.endpoint = endpoint;
        this.encryptedDHHalf = encryptedDHHalf;
        this.errorCode = errorCode;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public EncryptedDHHalf getDHHalf() {
        return encryptedDHHalf;
    }

    public byte getErrorCode() {
        return errorCode;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        endpoint.encode(buffer);
        encryptedDHHalf.encode(buffer);
        buffer.put(errorCode);
    }
}
