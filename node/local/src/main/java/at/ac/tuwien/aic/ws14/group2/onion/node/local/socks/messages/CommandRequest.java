package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.AddressTypeNotSupportedException;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.CommandNotSupportedException;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.MessageParsingException;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Created by klaus on 11/11/14.
 */
public class CommandRequest extends SocksMessage {
	private final Command command;
	private final SocksAddress destination;

	public CommandRequest(Command command, SocksAddress destination) {
		this.command = Objects.requireNonNull(command);
		this.destination = Objects.requireNonNull(destination);
	}

	/**
	 * Parses a byte array to a new instance of this class.
	 *
	 * @throws MessageParsingException  if the data cannot be parsed because it doesn't match the RFC 1928 specification
	 * @throws BufferUnderflowException if the byte array provided is shorter than the expected length
	 */
	public static CommandRequest fromByteArray(byte[] data) throws MessageParsingException, BufferUnderflowException, AddressTypeNotSupportedException, CommandNotSupportedException {
		Objects.requireNonNull(data);

		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(SocksMessage.NETWORK_BYTE_ORDER);

		byte version = bb.get();
		if (version != SocksMessage.VERSION)
			throw new MessageParsingException(String.format("wrong version byte: expected: 0x%02X; found: 0x%02X", SocksMessage.VERSION, version));

		// Convert the byte to the Command enumeration
		Command command = null;
		byte commandByte = bb.get();
		try {
			command = Command.fromByte(commandByte);
		} catch (IllegalArgumentException e) {
			throw new CommandNotSupportedException(commandByte, e);
		}

		byte reserved = bb.get();
		if (reserved != SocksMessage.RESERVED_BYTE)
			throw new MessageParsingException(String.format("wrong 'reserved' byte: expected: 0x%02X; found: 0x%02X", SocksMessage.RESERVED_BYTE, reserved));

		SocksAddress destination = SocksAddress.fromByteArray(bb);

		return new CommandRequest(command, destination);
	}

	public Command getCommand() {
		return command;
	}

	public SocksAddress getDestination() {
		return destination;
	}

	@Override
	public byte[] toByteArray() {
		byte[] addressBytes = destination.toByteArray();

		ByteBuffer bb = ByteBuffer.allocate(1 /* VER */ + 1 /* CMD */ + 1 /* RSV */ + addressBytes.length);

		bb.put(SocksMessage.VERSION);
		bb.put(command.getValue());
		bb.put(SocksMessage.RESERVED_BYTE);
		bb.put(addressBytes);

		return bb.array();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CommandRequest that = (CommandRequest) o;

		if (command != that.command) return false;
		if (destination != null ? !destination.equals(that.destination) : that.destination != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = command != null ? command.hashCode() : 0;
		result = 31 * result + (destination != null ? destination.hashCode() : 0);
		return result;
	}
}
