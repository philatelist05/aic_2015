package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions;

/**
 * Created by klaus on 11/18/14.
 */
public class MessageParsingException extends GeneralServerFailureException {
	public MessageParsingException() {
		super();
	}

	public MessageParsingException(String s) {
		super(s);
	}

	public MessageParsingException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public MessageParsingException(Throwable throwable) {
		super(throwable);
	}
}
