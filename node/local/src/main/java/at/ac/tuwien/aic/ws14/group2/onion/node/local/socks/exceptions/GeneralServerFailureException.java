package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions;

/**
 * Created by klaus on 11/18/14.
 */
public class GeneralServerFailureException extends SocksException {
	public GeneralServerFailureException() {
		super();
	}

	public GeneralServerFailureException(String s) {
		super(s);
	}

	public GeneralServerFailureException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public GeneralServerFailureException(Throwable throwable) {
		super(throwable);
	}
}
