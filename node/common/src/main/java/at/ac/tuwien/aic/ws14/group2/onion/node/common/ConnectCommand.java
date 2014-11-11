package at.ac.tuwien.aic.ws14.group2.onion.node.common;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 11.11.2014.
 */
public class ConnectCommand extends Command {

    private Inet4Address target;
    private int port;

    /**
     * Reads a Command assuming that the Command Type field has already been read.
     * The Command type will not be set.
     */
    ConnectCommand(ByteBuffer buffer) {
        try {
            byte[] ip = new byte[4];
            buffer.get(ip);
            target = (Inet4Address)Inet4Address.getByAddress(ip);
        } catch (UnknownHostException ex) {
            // IP address cannot be of invalid length.
        }

        port = buffer.getInt();
    }

    public ConnectCommand(Inet4Address target, int port) {
        super(COMMAND_TYPE_CONNECT);

        this.target = target;
        this.port = port;
    }

    public Inet4Address getTarget() {
        return target;
    }

    public int getPort() {
        return port;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        buffer.put(target.getAddress());
        buffer.putInt(port);
    }
}
