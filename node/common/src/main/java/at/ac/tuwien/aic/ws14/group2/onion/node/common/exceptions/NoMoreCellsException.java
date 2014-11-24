package at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions;

/**
 * Created by Thomas on 23.11.2014.
 */
public class NoMoreCellsException extends CellException {
    public NoMoreCellsException() {
    }

    public NoMoreCellsException(String msg) {
        super(msg);
    }

    public NoMoreCellsException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public NoMoreCellsException(Throwable cause) {
        super(cause);
    }
}
