package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

/**
 * Created by klaus on 11/11/14.
 */
public enum Command {
	CONNECT((byte) 0x01),
	BIND((byte) 0x02),
	UDP_ASSOCIATE((byte) 0x03);

	private byte value;

	private Command(byte value) {
		this.value = value;
	}

	public static Command fromByte(byte data) throws IllegalArgumentException {
		for (Command m : Command.values()) {
			if (m.value == data)
				return m;
		}
		throw new IllegalArgumentException(String.format("Unknown command 0x%02X", data));
	}

	public byte getValue() {
		return this.value;
	}
}
