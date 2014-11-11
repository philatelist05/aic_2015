package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

/**
 * Created by klaus on 11/11/14.
 */
public class MethodSelectionReply extends SocksMessage {
	private final Method method;

	public MethodSelectionReply(Method method) {
		this.method = method;
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
}
