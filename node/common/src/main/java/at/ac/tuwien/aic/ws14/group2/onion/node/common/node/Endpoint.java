package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Created by Thomas on 22.11.2014.
 *
 * Identifies a TCP/UDP endpoint.
 */
public class Endpoint {
    private static final byte TYPE_IPV4 = 1;
    private static final byte TYPE_IPV6 = 2;
    private static final byte TYPE_HOSTNAME = 3;

    private static final int LENGTH_IPV4 = 4;
    private static final int LENGTH_IPV6 = 16;

    private byte type;
    private InetAddress address;
    private String hostName;
    private int port;

    /**
     * Decodes an endpoint from the specified buffer.
     * @param source The position will be incremented by the number of bytes read from the buffer.
     * @throws DecodeException Thrown if the byte buffer does not contain a valid endpoint.
     */
    public Endpoint(ByteBuffer source) throws DecodeException {
        type = source.get();

        switch (type) {
            case TYPE_IPV4:
                address = parseAddressV4(source);
                break;
            case TYPE_IPV6:
                address = parseAddressV6(source);
                break;
            case TYPE_HOSTNAME:
                hostName = parseHostName(source);
                break;
            default:
                throw new DecodeException("Endpoint type " + type + " not supported.");
        }

        port = source.getInt();
    }

    private InetAddress parseAddressV4(ByteBuffer source) throws DecodeException  {
        byte[] buffer = new byte[LENGTH_IPV4];
        source.get(buffer);

        try {
            return Inet4Address.getByAddress(buffer);
        } catch (UnknownHostException e) {
            // cannot occur because length is correct
            return null;
        }
    }

    private InetAddress parseAddressV6(ByteBuffer source) throws DecodeException  {
        byte[] buffer = new byte[LENGTH_IPV6];
        source.get(buffer);

        try {
            return Inet6Address.getByAddress(buffer);
        } catch (UnknownHostException e) {
            // cannot occur because length is correct
            return null;
        }
    }

    private String parseHostName(ByteBuffer source) {
        int length = source.getInt();

        byte[] buffer = new byte[length];
        source.get(buffer);

        return new String(buffer);
    }

    public Endpoint(InetAddress address, int port) {
        this.address = Objects.requireNonNull(address);
        this.port = port;

        if (address instanceof Inet4Address)
            type = TYPE_IPV4;
        else
            type = TYPE_IPV6;
    }

    public Endpoint(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
        this.type = TYPE_HOSTNAME;
    }

    /**
     * Writes this endpoint to the specified buffer at the current position and increments the position by the number of written bytes.
     */
    public void encode(ByteBuffer destination) {
        destination.put(type);

        switch (type) {
            case TYPE_IPV4:
            case TYPE_IPV6:
                destination.put(address.getAddress());
                break;
            case TYPE_HOSTNAME: {
                byte[] buffer = hostName.getBytes();

                destination.putInt(buffer.length);
                destination.put(hostName.getBytes());
                break;
            }
        }

        destination.putInt(port);
    }

    /**
     * May do a DNS resolution.
     */
    public InetAddress getAddress() throws UnknownHostException {
        if (type == TYPE_HOSTNAME)
            return InetAddress.getByName(hostName);
        else
            return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        switch (type) {
            case TYPE_IPV4:
            case TYPE_IPV6:
                return Objects.hash(type, address, port);
            case TYPE_HOSTNAME:
                return Objects.hash(type, hostName, port);
        }

        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (obj.getClass() != getClass())
            return false;

        Endpoint other = (Endpoint)obj;

        if (type != other.type)
            return false;

        if (port != other.port)
            return false;

        switch (type) {
            case TYPE_IPV4:
            case TYPE_IPV6:
                return address.equals(other.address);
            case TYPE_HOSTNAME:
                return hostName.equals(other.hostName);
        }

        return false;
    }

    @Override
    public String toString() {
        return "Endpoint{" +
                "type=" + type +
                ", address=" + address +
                ", hostName='" + hostName + '\'' +
                ", port=" + port +
                '}';
    }
}
