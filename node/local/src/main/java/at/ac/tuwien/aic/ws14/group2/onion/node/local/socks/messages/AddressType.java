package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

/**
 * Created by klaus on 11/11/14.
 */
public enum AddressType {
	IP_V4_ADDRESS((byte) 0x01),
	DOMAINNAME((byte) 0x03),
	IP_V6_ADDRESS((byte) 0x04);

	private byte value;

	private AddressType(byte value) {
		this.value = value;
	}

	public static AddressType fromByte(byte data) throws IllegalArgumentException {
		for (AddressType m : AddressType.values()) {
			if (m.value == data)
				return m;
		}
		throw new IllegalArgumentException(String.format("Unknown address type 0x%02X", data));
	}

	public byte getValue() {
		return this.value;
	}
}
