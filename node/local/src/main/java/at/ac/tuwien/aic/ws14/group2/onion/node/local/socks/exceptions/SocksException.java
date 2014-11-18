package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions;

/**
 * Created by klaus on 11/18/14.
 */
public abstract class SocksException extends Exception {
	public SocksException() {
		super();
	}

	public SocksException(String s) {
		super(s);
	}

	public SocksException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public SocksException(Throwable throwable) {
		super(throwable);
	}
}
