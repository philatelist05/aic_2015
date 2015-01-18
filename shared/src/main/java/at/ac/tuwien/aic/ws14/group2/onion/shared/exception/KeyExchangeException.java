package at.ac.tuwien.aic.ws14.group2.onion.shared.exception;

public class KeyExchangeException extends Exception {
    public KeyExchangeException() {
        super();
    }

    public KeyExchangeException(String message) {
        super(message);
    }

    public KeyExchangeException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyExchangeException(Throwable cause) {
        super(cause);
    }

    protected KeyExchangeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
