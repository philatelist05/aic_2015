package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import java.util.Objects;

/**
 * Created by klaus on 11/11/14.
 */
public class MethodSelectionReply extends SocksMessage {
	private final Method method;

	public MethodSelectionReply(Method method) {
		this.method = Objects.requireNonNull(method);
	}

	public static MethodSelectionReply fromByteArray(byte[] data) {
		// TODO (KK) Implement method selection reply message parsing
		return null;
	}

	public Method getMethod() {
		return method;
	}

	@Override
	public byte[] toByteArray() {
		// TODO (KK) Implement method selection reply serialization
		return new byte[0];
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
