package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * Created by Thomas on 24.11.2014.
 */
public class EndpointTest {
    private static final byte[] ADDRESS_V4 = new byte[] {1, 2, 3, 4};
    private static final byte[] ADDRESS_V6 = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
    private static final String HOSTNAME = "www.google.at";

    private Endpoint encodeDecode(Endpoint e) throws DecodeException {
        byte[] ar = new byte[200];
        ByteBuffer buffer = ByteBuffer.wrap(ar);

        e.encode(buffer);

        buffer.flip();
        return new Endpoint(buffer);
    }

    @Test
    public void encodeDecodeV4() throws UnknownHostException, DecodeException {
        Endpoint e1 = new Endpoint(InetAddress.getByAddress(new byte[] {1, 2, 3, 4}), 100);
        Endpoint e2 = encodeDecode(e1);

        assertEquals(e1, e2);
    }

    @Test
    public void encodeDecodeV6() throws UnknownHostException, DecodeException {
        Endpoint e1 = new Endpoint(InetAddress.getByAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}), 100);
        Endpoint e2 = encodeDecode(e1);

        assertEquals(e1, e2);
    }

    @Test
    public void encodeDecodeHostName() throws UnknownHostException, DecodeException {
        Endpoint e1 = new Endpoint("www.google.at", 100);
        Endpoint e2 = encodeDecode(e1);

        assertEquals(e1, e2);
    }

    @Test
    public void testEquals() throws UnknownHostException {
        assertEquals(new Endpoint(InetAddress.getByAddress(new byte[] {1, 2, 3, 4}), 100), new Endpoint(InetAddress.getByAddress(new byte[] {1, 2, 3, 4}), 100));
        assertNotEquals(new Endpoint(InetAddress.getByAddress(new byte[] {99, 2, 3, 4}), 100), new Endpoint(InetAddress.getByAddress(new byte[] {1, 2, 3, 4}), 100));
        assertNotEquals(new Endpoint(InetAddress.getByAddress(new byte[] {1, 2, 3, 4}), 999), new Endpoint(InetAddress.getByAddress(new byte[] {1, 2, 3, 4}), 100));

        assertEquals(new Endpoint(InetAddress.getByAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}), 100), new Endpoint(InetAddress.getByAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}), 100));
        assertNotEquals(new Endpoint(InetAddress.getByAddress(new byte[]{99, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}), 100), new Endpoint(InetAddress.getByAddress(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}), 100));
        assertNotEquals(new Endpoint(InetAddress.getByAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}), 999), new Endpoint(InetAddress.getByAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}), 100));

        assertEquals(new Endpoint("www.google.at", 100), new Endpoint("www.google.at", 100));
        assertNotEquals(new Endpoint("www.amazon.at", 100), new Endpoint("www.google.at", 100));
        assertNotEquals(new Endpoint("www.google.at", 999), new Endpoint("www.google.at", 100));

        assertNotEquals(new Endpoint("www.amazon.at", 100), new Endpoint(InetAddress.getByAddress(new byte[] {99, 2, 3, 4}), 100));
        assertNotEquals(new Endpoint(InetAddress.getByAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}), 100), new Endpoint("www.google.at", 100));
        assertNotEquals(new Endpoint(InetAddress.getByAddress(new byte[] {99, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}), 100), new Endpoint(InetAddress.getByAddress(new byte[] {99, 2, 3, 4}), 100));
    }

    @Test
    public void testHashCode() throws UnknownHostException {
        assertEquals(new Endpoint(InetAddress.getByAddress(new byte[] {1, 2, 3, 4}), 100).hashCode(), new Endpoint(InetAddress.getByAddress(new byte[] {1, 2, 3, 4}), 100).hashCode());
        assertEquals(new Endpoint(InetAddress.getByAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}), 100).hashCode(), new Endpoint(InetAddress.getByAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}), 100).hashCode());
        assertEquals(new Endpoint("www.google.at", 100).hashCode(), new Endpoint("www.google.at", 100).hashCode());
    }
}
