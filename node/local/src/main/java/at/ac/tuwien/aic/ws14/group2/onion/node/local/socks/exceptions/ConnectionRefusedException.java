package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages.ReplyType;

/**
 * Created by klaus on 11/18/14.
 */
public class ConnectionRefusedException extends SocksException {
	public ConnectionRefusedException() {
		super();
	}

	public ConnectionRefusedException(String s) {
		super(s);
	}

	public ConnectionRefusedException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public ConnectionRefusedException(Throwable throwable) {
		super(throwable);
	}

	@Override
	public ReplyType getReplyType() {
		return ReplyType.CONNECTION_REFUSED;
	}
}
