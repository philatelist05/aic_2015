package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.*;

/**
 * Created by klaus on 11/11/14.
 */
public enum ReplyType {
	SUCCEEDED((byte) 0x00, null),
	GENERAL_SERVER_FAILURE((byte) 0x01, GeneralServerFailureException.class),
	CONNECTION_NOT_ALLOWED((byte) 0x02, ConnectionNotAllowedException.class),
	NETWORK_UNREACHABLE((byte) 0x03, NetworkUnreachableException.class),
	HOST_UNREACHABLE((byte) 0x04, HostUnreachableException.class),
	CONNECTION_REFUSED((byte) 0x05, ConnectionRefusedException.class),
	TTL_EXPIRED((byte) 0x06, TtlExpiredException.class),
	COMMAND_NOT_SUPPORTED((byte) 0x07, CommandNotSupportedException.class),
	ADDRESS_TYPE_NOT_SUPPORTED((byte) 0x08, AddressTypeNotSupportedException.class);


	private final byte value;
	private final Class<? extends SocksException> exceptionClass;

	private ReplyType(byte value, Class<? extends SocksException> exceptionClass) {
		this.value = value;
		this.exceptionClass = exceptionClass;
	}

	public static ReplyType fromByte(byte data) throws IllegalArgumentException {
		for (ReplyType m : ReplyType.values()) {
			if (m.value == data)
				return m;
		}
		throw new IllegalArgumentException(String.format("Unknown rely type 0x%02X", data));
	}

	public Class<? extends SocksException> getExceptionClass() {
		return exceptionClass;
	}

	public byte getValue() {
		return this.value;
	}
}
