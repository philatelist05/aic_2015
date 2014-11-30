package at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions;

/**
 * Created by Thomas on 22.11.2014.
 */
public class EncryptException extends Exception {
    public EncryptException() {
    }

    public EncryptException(String msg) {
        super(msg);
    }

    public EncryptException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public EncryptException(Throwable cause) {
        super(cause);
    }
}
