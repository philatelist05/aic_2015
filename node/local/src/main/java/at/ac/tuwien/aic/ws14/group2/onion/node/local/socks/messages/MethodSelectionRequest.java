package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.ParseMessageException;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * Created by klaus on 11/11/14.
 */
public class MethodSelectionRequest extends SocksMessage {
	private final Method[] methods;

	public MethodSelectionRequest(Method... methods) {
		this.methods = Objects.requireNonNull(methods);

		if (methods.length > 0xFF)
			throw new IllegalArgumentException("no more than " + 0xFF + " methods allowed");
	}

	public static MethodSelectionRequest fromByteArray(byte[] data) throws ParseMessageException, BufferUnderflowException {
		Objects.requireNonNull(data);

		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(SocksMessage.NETWORK_BYTE_ORDER);

		byte version = bb.get();
		if (version != SocksMessage.VERSION)
			throw new ParseMessageException(String.format("wrong version byte: 0x%X expected to be 0x%X", version, SocksMessage.VERSION));

		int length = Byte.toUnsignedInt(bb.get());
		byte[] methodsBytes = new byte[length];
		bb.get(methodsBytes);

		Method[] methods = new Method[length];

		// Convert the bytes to the enumeration
		try {
			for (int i = 0; i < methodsBytes.length; i++) {
				methods[i] = Method.fromByte(methodsBytes[i]);
			}
		} catch (IllegalArgumentException e) {
			throw new ParseMessageException(e);
		}

		return new MethodSelectionRequest(methods);
	}

	public Method[] getMethods() {
		return methods;
	}

	@Override
	public byte[] toByteArray() throws BufferOverflowException {

		ByteBuffer bb = ByteBuffer.allocate(1 /* VER */ + methods.length);

		bb.put(SocksMessage.VERSION);
		bb.put((byte) methods.length);

		for (Method m : methods) {
			bb.put(m.getValue());
		}

		return bb.array();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MethodSelectionRequest that = (MethodSelectionRequest) o;

		if (!Arrays.equals(methods, that.methods)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return methods != null ? Arrays.hashCode(methods) : 0;
	}
}
