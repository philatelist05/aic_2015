package at.ac.tuwien.aic.ws14.group2.onion.directory.exceptions;

/**
 * Created by Stefan on 20.01.15.
 */
public class NoSuchChainNodeAvailable extends Exception {
    public NoSuchChainNodeAvailable() {
    }

    public NoSuchChainNodeAvailable(String msg) {
        super(msg);
    }

    public NoSuchChainNodeAvailable(String msg, Throwable cause) {
        super(msg, cause);
    }

    public NoSuchChainNodeAvailable(Throwable cause) {
        super(cause);
    }
}
