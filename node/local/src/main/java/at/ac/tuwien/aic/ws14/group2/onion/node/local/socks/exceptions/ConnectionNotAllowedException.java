package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages.ReplyType;

/**
 * Created by klaus on 11/18/14.
 */
public class ConnectionNotAllowedException extends SocksException {
	public ConnectionNotAllowedException() {
		super();
	}

	public ConnectionNotAllowedException(String s) {
		super(s);
	}

	public ConnectionNotAllowedException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public ConnectionNotAllowedException(Throwable throwable) {
		super(throwable);
	}

	@Override
	public ReplyType getReplyType() {
		return ReplyType.CONNECTION_NOT_ALLOWED;
	}
}
