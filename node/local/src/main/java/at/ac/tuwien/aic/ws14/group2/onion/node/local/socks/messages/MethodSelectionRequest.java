package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by klaus on 11/11/14.
 */
public class MethodSelectionRequest extends SocksMessage {
	private final Method[] methods;

	public MethodSelectionRequest(Method... methods) {
		this.methods = Objects.requireNonNull(methods);
	}

	public static MethodSelectionReply fromByteArray(byte[] data) {
		// TODO (KK) Implement method selection request message parsing
		return null;
	}

	public Method[] getMethods() {
		return methods;
	}

	@Override
	public byte[] toByteArray() {
		// TODO (KK) Implement method selection request serialization
		return new byte[0];
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
