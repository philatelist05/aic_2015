package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.AddressTypeNotSupportedException;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.MessageParsingException;

import java.io.*;
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
	 * @return a new instance of this class
	 * @throws java.io.EOFException             if the input provided is shorter than the expected length
	 * @throws AddressTypeNotSupportedException if an invalid address type byte was provided
	 */
	public static CommandReply fromByteArray(byte[] data) throws EOFException, AddressTypeNotSupportedException, MessageParsingException {
		Objects.requireNonNull(data);

		try {
			return fromByteArray(new DataInputStream(new ByteArrayInputStream(data)));
		} catch (IOException e) {
			if (e instanceof EOFException)
				throw (EOFException) e;
			// Should never be the case since we are reading from an byte array
			throw new RuntimeException();
		}
	}

	/**
	 * Parses a byte array to a new instance of this class.
	 *
	 * @throws MessageParsingException if the data cannot be parsed because it doesn't match the RFC 1928 specification
	 * @throws EOFException            if the input provided is shorter than the expected length
	 */
	public static CommandReply fromByteArray(DataInput input) throws MessageParsingException, AddressTypeNotSupportedException, IOException {
		Objects.requireNonNull(input);

		byte version = input.readByte();
		if (version != SocksMessage.VERSION)
			throw new MessageParsingException(String.format("wrong version byte: expected: 0x%02X; found: 0x%02X", SocksMessage.VERSION, version));

		// Convert the byte to the ReplyType enumeration
		ReplyType replyType;
		try {
			replyType = ReplyType.fromByte(input.readByte());
		} catch (IllegalArgumentException e) {
			throw new MessageParsingException("reply type not found", e);
		}

		byte reserved = input.readByte();
		if (reserved != SocksMessage.RESERVED_BYTE)
			throw new MessageParsingException(String.format("wrong 'reserved' byte: expected: 0x%02X; found: 0x%02X", SocksMessage.RESERVED_BYTE, reserved));

		SocksAddress boundAddress = SocksAddress.fromByteArray(input);

		return new CommandReply(replyType, boundAddress);
	}

	public ReplyType getReplyType() {
		return replyType;
	}

	public SocksAddress getBoundAddress() {
		return boundAddress;
	}

	@Override
	public byte[] toByteArray() {
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
