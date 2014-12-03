package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 11.11.2014.
 */
public class ConnectCommand extends Command {

    private Endpoint endpoint;

    /**
     * Reads a Command assuming that the Command Type field has already been read.
     * The Command type will not be set.
     */
    ConnectCommand(ByteBuffer buffer) throws DecodeException {
        endpoint = new Endpoint(buffer);
    }

    public ConnectCommand(Endpoint endpoint) {
        super(COMMAND_TYPE_CONNECT);
        this.endpoint = endpoint;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        endpoint.encode(buffer);
    }

    @Override
    public String toString() {
        return "ConnectCommand{" +
                "endpoint=" + endpoint +
                '}';
    }
}
