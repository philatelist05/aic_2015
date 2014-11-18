package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import java.util.Objects;

/**
 * Created by klaus on 11/11/14.
 */
public class CommandReply extends SocksMessage {
	private final ReplyType replyType;
	private final SocksAddress boundAddress;

	public CommandReply(ReplyType replyType, SocksAddress boundAddress) {
		this.replyType = Objects.requireNonNull(replyType);
		this.boundAddress = Objects.requireNonNull(boundAddress);
	}

	public static CommandReply fromByteArray(byte[] data) {
		// TODO (KK) Implement command reply message parsing
		return null;
	}

	public ReplyType getReplyType() {
		return replyType;
	}

	public SocksAddress getBoundAddress() {
		return boundAddress;
	}

	@Override
	public byte[] toByteArray() {
		// TODO (KK) Implement command reply serialization
		return new byte[0];
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CommandReply that = (CommandReply) o;

		if (boundAddress != null ? !boundAddress.equals(that.boundAddress) : that.boundAddress != null) return false;
		if (replyType != that.replyType) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = replyType != null ? replyType.hashCode() : 0;
		result = 31 * result + (boundAddress != null ? boundAddress.hashCode() : 0);
		return result;
	}
}
