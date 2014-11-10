package at.ac.tuwien.aic.ws14.group2.onion.node.common;

import java.net.*;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 09.11.2014.
 */
public class ExtendCommand extends Command {

    private InetAddress target;
    private byte[] encryptedDiffieHalf;

    /**
     * Reads a Command assuming that the Command field has already been read.
     */
    public ExtendCommand(ByteBuffer buffer) {
        try {
            byte[] ip = new byte[4];
            buffer.get(ip);
            target = InetAddress.getByAddress(ip);
        } catch (UnknownHostException ex) {
            // IP address cannot be of invalid length.
        }

        encryptedDiffieHalf = new byte[Cell.DIFFIE_HELLMAN_HALF_BYTES];
        buffer.get(encryptedDiffieHalf);
    }

    public InetAddress getTarget() {
        return target;
    }

    public byte[] getDiffieHellmanHalf(byte[] privateKey) {
        // TODO: decrypt
        return encryptedDiffieHalf;
    }

    @Override
    protected void encodePayload(ByteBuffer buffer) {
        buffer.put(target.getAddress());
        buffer.put(encryptedDiffieHalf);
    }
}
