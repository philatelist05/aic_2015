package at.ac.tuwien.aic.ws14.group2.onion.shared.exception;

/**
 * Created by Thomas on 22.11.2014.
 */
public class DecryptException extends Exception {
    public DecryptException() {
    }

    public DecryptException(String msg) {
        super(msg);
    }

    public DecryptException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public DecryptException(Throwable cause) {
        super(cause);
    }
}
