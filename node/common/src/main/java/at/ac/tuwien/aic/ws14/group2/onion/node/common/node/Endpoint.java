package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import java.net.InetAddress;
import java.util.Objects;

/**
 * Created by Thomas on 22.11.2014.
 */
public class Endpoint {
    private InetAddress address;
    private int port;

    public Endpoint(InetAddress address, int port) {
        this.address = Objects.requireNonNull(address);
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
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
        return address.equals(other.address) && port == other.port;
    }

    @Override
    public String toString() {
        return "Endpoint{" +
                "address=" + address +
                ", port=" + port +
                '}';
    }
}
