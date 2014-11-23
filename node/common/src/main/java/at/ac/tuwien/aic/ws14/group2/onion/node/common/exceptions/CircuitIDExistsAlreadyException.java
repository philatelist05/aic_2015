package at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions;

/**
 * Created by Thomas on 22.11.2014.
 */
public class CircuitIDExistsAlreadyException extends Exception {
    public CircuitIDExistsAlreadyException() {
    }

    public CircuitIDExistsAlreadyException(String msg) {
        super(msg);
    }

    public CircuitIDExistsAlreadyException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public CircuitIDExistsAlreadyException(Throwable cause) {
        super(cause);
    }
}
