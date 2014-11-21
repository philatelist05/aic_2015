package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.AddressTypeNotSupportedException;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.MessageParsingException;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
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

	/**
	 * Parses a byte array to a new instance of this class.
	 *
	 * @throws MessageParsingException  if the data cannot be parsed because it doesn't match the RFC 1928 specification
	 * @throws BufferUnderflowException if the byte array provided is shorter than the expected length
	 */
	public static CommandReply fromByteArray(byte[] data) throws MessageParsingException, BufferUnderflowException, AddressTypeNotSupportedException {
		Objects.requireNonNull(data);

		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(SocksMessage.NETWORK_BYTE_ORDER);

		byte version = bb.get();
		if (version != SocksMessage.VERSION)
			throw new MessageParsingException(String.format("wrong version byte: expected: 0x%02X; found: 0x%02X", SocksMessage.VERSION, version));

		// Convert the byte to the ReplyType enumeration
		ReplyType replyType = null;
		try {
			replyType = ReplyType.fromByte(bb.get());
		} catch (IllegalArgumentException e) {
			throw new MessageParsingException("reply type not found", e);
		}

		byte reserved = bb.get();
		if (reserved != SocksMessage.RESERVED_BYTE)
			throw new MessageParsingException(String.format("wrong 'reserved' byte: expected: 0x%02X; found: 0x%02X", SocksMessage.RESERVED_BYTE, reserved));

		SocksAddress boundAddress = SocksAddress.fromByteArray(bb);

		return new CommandReply(replyType, boundAddress);
	}

	public ReplyType getReplyType() {
		return replyType;
	}

	public SocksAddress getBoundAddress() {
		return boundAddress;
	}

	@Override
	public byte[] toByteArray() throws BufferOverflowException {
		byte[] addressBytes = boundAddress.toByteArray();

		ByteBuffer bb = ByteBuffer.allocate(1 /* VER */ + 1 /* REP */ + 1 /* RSV */ + addressBytes.length);

		bb.put(SocksMessage.VERSION);
		bb.put(replyType.getValue());
		bb.put(SocksMessage.RESERVED_BYTE);
		bb.put(addressBytes);

		return bb.array();
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
