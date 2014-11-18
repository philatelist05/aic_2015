package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions;

/**
 * Created by klaus on 11/18/14.
 */
public class CommandNotSupportedException extends SocksException {
	public CommandNotSupportedException() {
		super();
	}

	public CommandNotSupportedException(String s) {
		super(s);
	}

	public CommandNotSupportedException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public CommandNotSupportedException(Throwable throwable) {
		super(throwable);
	}
}
