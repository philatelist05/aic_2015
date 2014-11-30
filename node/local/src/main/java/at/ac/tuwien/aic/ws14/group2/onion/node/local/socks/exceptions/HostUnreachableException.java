package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages.ReplyType;

/**
 * Created by klaus on 11/18/14.
 */
public class HostUnreachableException extends SocksException {
	public HostUnreachableException() {
		super();
	}

	public HostUnreachableException(String s) {
		super(s);
	}

	public HostUnreachableException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public HostUnreachableException(Throwable throwable) {
		super(throwable);
	}

	@Override
	public ReplyType getReplyType() {
		return ReplyType.HOST_UNREACHABLE;
	}
}
