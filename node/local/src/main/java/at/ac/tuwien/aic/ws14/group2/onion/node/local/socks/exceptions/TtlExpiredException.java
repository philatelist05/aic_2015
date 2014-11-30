package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages.ReplyType;

/**
 * Created by klaus on 11/18/14.
 */
public class TtlExpiredException extends SocksException {
	public TtlExpiredException() {
		super();
	}

	public TtlExpiredException(String s) {
		super(s);
	}

	public TtlExpiredException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public TtlExpiredException(Throwable throwable) {
		super(throwable);
	}

	@Override
	public ReplyType getReplyType() {
		return ReplyType.TTL_EXPIRED;
	}
}
