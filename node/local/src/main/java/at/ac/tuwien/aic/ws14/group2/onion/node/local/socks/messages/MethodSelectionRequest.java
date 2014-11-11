package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

/**
 * Created by klaus on 11/11/14.
 */
public class MethodSelectionRequest extends SocksMessage {
	private final Method[] methods;

	public MethodSelectionRequest(Method[] methods) {
		this.methods = methods;
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
}
