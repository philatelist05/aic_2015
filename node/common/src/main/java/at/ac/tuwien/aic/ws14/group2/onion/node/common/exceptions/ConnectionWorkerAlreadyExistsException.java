package at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions;

/**
 * Created by Thomas on 22.11.2014.
 */
public class ConnectionWorkerAlreadyExistsException extends Exception {
    public ConnectionWorkerAlreadyExistsException() {
    }

    public ConnectionWorkerAlreadyExistsException(String msg) {
        super(msg);
    }

    public ConnectionWorkerAlreadyExistsException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ConnectionWorkerAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
