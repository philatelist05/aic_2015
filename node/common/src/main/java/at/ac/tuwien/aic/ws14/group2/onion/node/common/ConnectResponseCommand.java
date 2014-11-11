package at.ac.tuwien.aic.ws14.group2.onion.node.common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 11.11.2014.
 */
public class ConnectResponseCommand extends Command {
    public ConnectResponseCommand() {
        super(Command.COMMAND_TYPE_CONNECT_RESPONSE);
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
    }
}
