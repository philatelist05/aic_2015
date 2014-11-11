package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

/**
 * Created by klaus on 11/11/14.
 */
public enum ReplyType {
	SUCCEEDED((byte) 0x00),
	GENERAL_SERVER_FAILURE((byte) 0x01),
	CONNECTION_NOT_ALLOWED((byte) 0x02),
	NETWORK_UNREACHABLE((byte) 0x03),
	HOST_UNREACHABLE((byte) 0x04),
	CONNECTION_REFUSED((byte) 0x05),
	TTL_EXPIRED((byte) 0x06),
	COMMAND_NOT_SUPPORTED((byte) 0x07),
	ADDRESS_TYPE_NOT_SUPPORTED((byte) 0x08);


	private byte value;

	private ReplyType(byte value) {
		this.value = value;
	}

	public static ReplyType fromByte(byte data) throws EnumConstantNotPresentException {
		for (ReplyType m : ReplyType.values()) {
			if (m.value == data)
				return m;
		}
		throw new EnumConstantNotPresentException(ReplyType.class, String.format("0x%02X", data));
	}

	public byte getValue() {
		return this.value;
	}
}
