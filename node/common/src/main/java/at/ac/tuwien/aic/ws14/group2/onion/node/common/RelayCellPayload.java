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
     * Creates a Relay Cell Payload by encoding the specified command.
     * @param command
     */
    public RelayCellPayload(Command command) {

    }

    public RelayCellPayload decrypt(byte[] sessionKey) {
        // TODO: decrypt payload
        return new RelayCellPayload(payload);
    }

    /**
     * Decodes this Relay Payload assuming that it is not encrypted.
     */
    public Command decode() throws DecodeException {
        return Command.decode(payload);
    }
}
