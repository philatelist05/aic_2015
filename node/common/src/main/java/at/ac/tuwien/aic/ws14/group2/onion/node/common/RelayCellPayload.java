package at.ac.tuwien.aic.ws14.group2.onion.node.common;

/**
 * Created by Thomas on 09.11.2014.
 */
public class RelayCellPayload {
    private byte[] payload;

    public RelayCellPayload(byte[] payload) {
        this.payload = payload;
    }

    /**
     * Creates a Relay Cell Payload by encoding the specified Command.
     * Changes in the Command object will not be reflected in this payload.
     */
    public RelayCellPayload(Command command) {
        payload = command.encode();
    }

    public RelayCellPayload decrypt(byte[] sessionKey) {
        // TODO: decrypt payload
        return new RelayCellPayload(payload);
    }

    public RelayCellPayload encrypt(byte[] sessionKey) {
        // TODO: encrypt payload
        return new RelayCellPayload(payload);
    }

    /**
     * Decodes this Relay Payload assuming that it is not encrypted.
     */
    public Command decode() throws DecodeException {
        return Command.decode(payload);
    }

    /**
     * @return A byte array of size Cell.CELL_PAYLOAD_BYTES.
     */
    public byte[] encode() {
        return payload;
    }
}
