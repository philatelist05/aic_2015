package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import java.nio.BufferOverflowException;
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

	public static MethodSelectionReply fromByteArray(byte[] data) {
		// TODO (KK) Implement method selection request message parsing
		return null;
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
