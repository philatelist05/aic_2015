package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import java.nio.ByteBuffer;

/**
 * Created by Thomas on 11.11.2014.
 */
public class ConnectResponseCommand extends Command {
    public ConnectResponseCommand() {
        super(COMMAND_TYPE_CONNECT_RESPONSE);
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
    }
}
