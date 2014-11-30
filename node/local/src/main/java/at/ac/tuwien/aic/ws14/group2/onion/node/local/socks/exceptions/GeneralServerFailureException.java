package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages.ReplyType;

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

	@Override
	public ReplyType getReplyType() {
		return ReplyType.GENERAL_SERVER_FAILURE;
	}
}
