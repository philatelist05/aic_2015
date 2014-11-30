package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.AddressTypeNotSupportedException;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.CommandNotSupportedException;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.MessageParsingException;

import java.io.*;
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
	 * @return a new instance of this class
	 * @throws java.io.EOFException             if the input provided is shorter than the expected length
	 * @throws AddressTypeNotSupportedException if an invalid address type byte was provided
	 */
	public static CommandRequest fromByteArray(byte[] data) throws EOFException, AddressTypeNotSupportedException, CommandNotSupportedException, MessageParsingException {
		Objects.requireNonNull(data);

		try {
			return fromInputStream(new DataInputStream(new ByteArrayInputStream(data)));
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
	public static CommandRequest fromInputStream(DataInput input) throws MessageParsingException, AddressTypeNotSupportedException, CommandNotSupportedException, IOException {
		Objects.requireNonNull(input);

		byte version = input.readByte();
		if (version != SocksMessage.VERSION)
			throw new MessageParsingException(String.format("wrong version byte: expected: 0x%02X; found: 0x%02X", SocksMessage.VERSION, version));

		// Convert the byte to the Command enumeration
		Command command;
		byte commandByte = input.readByte();
		try {
			command = Command.fromByte(commandByte);
		} catch (IllegalArgumentException e) {
			throw new CommandNotSupportedException(commandByte, e);
		}

		byte reserved = input.readByte();
		if (reserved != SocksMessage.RESERVED_BYTE)
			throw new MessageParsingException(String.format("wrong 'reserved' byte: expected: 0x%02X; found: 0x%02X", SocksMessage.RESERVED_BYTE, reserved));

		SocksAddress destination = SocksAddress.fromInputStream(input);

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
