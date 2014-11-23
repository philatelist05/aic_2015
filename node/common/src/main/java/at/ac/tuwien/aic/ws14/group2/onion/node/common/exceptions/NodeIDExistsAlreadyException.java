package at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions;

/**
 * Created by Thomas on 22.11.2014.
 */
public class NodeIDExistsAlreadyException extends Exception {
    public NodeIDExistsAlreadyException() {
    }

    public NodeIDExistsAlreadyException(String msg) {
        super(msg);
    }

    public NodeIDExistsAlreadyException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public NodeIDExistsAlreadyException(Throwable cause) {
        super(cause);
    }
}
