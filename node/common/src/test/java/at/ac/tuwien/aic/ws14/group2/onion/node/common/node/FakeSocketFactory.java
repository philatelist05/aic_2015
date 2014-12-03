package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Stefan on 03.12.14.
 */
public class FakeSocketFactory extends SocketFactory {

    private final Socket socket;

    public FakeSocketFactory(Socket socket) {
        super();
        this.socket = socket;
    }

    @Override
    public Socket createSocket() throws IOException {
        return socket;
    }

    @Override
    public Socket createSocket(String s, int i) throws IOException, UnknownHostException {
        return socket;
    }

    @Override
    public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException, UnknownHostException {
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException {
        return socket;
    }
}
