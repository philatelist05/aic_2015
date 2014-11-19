package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions;

/**
 * Created by klaus on 11/18/14.
 */
public class ParseMessageException extends GeneralServerFailureException {
	public ParseMessageException() {
		super();
	}

	public ParseMessageException(String s) {
		super(s);
	}

	public ParseMessageException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public ParseMessageException(Throwable throwable) {
		super(throwable);
	}
}
