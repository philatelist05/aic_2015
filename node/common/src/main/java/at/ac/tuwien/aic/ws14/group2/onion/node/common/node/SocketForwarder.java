package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

/**
 * Created by lubuntu on 02.12.14.
 */
public class SocketForwarder implements TargetForwarder {

    private final Endpoint endpoint;

    public SocketForwarder(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void forward(byte[] data) {
    }
}
