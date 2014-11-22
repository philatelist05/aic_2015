package at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions;

/**
 * Created by Thomas on 22.11.2014.
 */
public class CellException extends Exception {
    public CellException() {
    }

    public CellException(String msg) {
        super(msg);
    }

    public CellException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public CellException(Throwable cause) {
        super(cause);
    }
}
