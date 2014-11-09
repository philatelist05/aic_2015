package at.ac.tuwien.aic.ws14.group2.onion.node.common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created by Thomas on 09.11.2014.
 */
public class ExtendResponseCommand extends Command {

    private byte[] diffieHalf;
    private byte[] signature;

    /**
     * Reads a Command assuming that the Command field has already been read.
     */
    public ExtendResponseCommand(ByteBuffer buffer) {
        diffieHalf = new byte[Cell.DIFFIE_HELLMAN_HALF_BYTES];
        signature = new byte[Cell.SIGNATURE_BYTES];

        buffer.get(diffieHalf);
        buffer.get(signature);
    }

    public byte[] getDiffieHellmanHalf() {
        return diffieHalf;
    }

    public byte[] getSignature() {
        return signature;
    }
}
