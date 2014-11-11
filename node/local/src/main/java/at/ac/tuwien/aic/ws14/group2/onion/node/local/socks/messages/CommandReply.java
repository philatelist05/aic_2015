package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

/**
 * Created by klaus on 11/11/14.
 */
public class CommandReply extends SocksMessage {
	private final ReplyType replyType;
	private final SocksAddress boundAddress;

	public CommandReply(ReplyType replyType, SocksAddress boundAddress) {
		this.replyType = replyType;
		this.boundAddress = boundAddress;
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
}
