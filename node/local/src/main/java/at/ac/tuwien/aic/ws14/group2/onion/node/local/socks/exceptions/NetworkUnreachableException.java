package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages.ReplyType;

/**
 * Created by klaus on 11/18/14.
 */
public class NetworkUnreachableException extends SocksException {
	public NetworkUnreachableException() {
		super();
	}

	public NetworkUnreachableException(String s) {
		super(s);
	}

	public NetworkUnreachableException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public NetworkUnreachableException(Throwable throwable) {
		super(throwable);
	}

	@Override
	public ReplyType getReplyType() {
		return ReplyType.NETWORK_UNREACHABLE;
	}
}
