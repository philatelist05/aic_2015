package at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions;

/**
 * Created by Thomas on 22.11.2014.
 */
public class WrongCircuitIDException extends Exception {
    public WrongCircuitIDException() {
    }

    public WrongCircuitIDException(String msg) {
        super(msg);
    }

    public WrongCircuitIDException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public WrongCircuitIDException(Throwable cause) {
        super(cause);
    }
}
