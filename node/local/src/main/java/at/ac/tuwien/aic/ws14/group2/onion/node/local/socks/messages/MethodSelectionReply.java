package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.MessageParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Objects;

/**
 * Created by klaus on 11/11/14.
 */
public class MethodSelectionReply extends SocksMessage {
	private static final Logger logger = LoggerFactory.getLogger(MethodSelectionReply.class.getName());

	private final Method method;

	public MethodSelectionReply(Method method) {
		this.method = Objects.requireNonNull(method);
	}

	/**
	 * Parses a byte array to a new instance of this class.
	 *
	 * @return a new instance of this class
	 * @throws java.io.EOFException if the input provided is shorter than the expected length
	 */
	public static MethodSelectionReply fromByteArray(byte[] data) throws EOFException, MessageParsingException {
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
	public static MethodSelectionReply fromInputStream(DataInput input) throws MessageParsingException, IOException {
		Objects.requireNonNull(input);

		byte version = input.readByte();
		if (version != SocksMessage.VERSION)
			throw new MessageParsingException(String.format("wrong version byte: expected: 0x%02X; found: 0x%02X", SocksMessage.VERSION, version));

		// Convert the byte to the Method enumeration
		Method method;
		try {
			method = Method.fromByte(input.readByte());
		} catch (IllegalArgumentException e) {
			method = Method.UNKNOWN_METHOD;
			logger.debug(e.getMessage());
		}

		return new MethodSelectionReply(method);
	}

	public Method getMethod() {
		return method;
	}

	@Override
	public byte[] toByteArray() {
		return new byte[]{SocksMessage.VERSION, method.getValue()};
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MethodSelectionReply that = (MethodSelectionReply) o;

		if (method != that.method) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return method != null ? method.hashCode() : 0;
	}
}
