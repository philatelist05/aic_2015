package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

/**
 * Created by klaus on 11/11/14.
 */
public enum Method {
	NO_AUTHENTICATION_REQUIRED((byte) 0x00),
	GSSAPI((byte) 0x01),
	USERNAME_PASSWORD((byte) 0x02),
	NO_ACCEPTABLE_METHODS((byte) 0xFF);

	private byte value;

	private Method(byte value) {
		this.value = value;
	}

	public static Method fromByte(byte data) throws EnumConstantNotPresentException {
		for (Method m : Method.values()) {
			if (m.value == data)
				return m;
		}
		throw new EnumConstantNotPresentException(Method.class, String.format("0x%02X", data));
	}

	public byte getValue() {
		return this.value;
	}
}
