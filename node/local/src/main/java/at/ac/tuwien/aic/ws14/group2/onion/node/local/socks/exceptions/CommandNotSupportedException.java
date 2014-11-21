package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions;

/**
 * Created by klaus on 11/18/14.
 */
public class CommandNotSupportedException extends SocksException {
	private final byte commandByte;

	public CommandNotSupportedException(byte commandByte) {
		super(String.format("command 0x%02X not supported", commandByte));
		this.commandByte = commandByte;
	}

	public CommandNotSupportedException(byte commandByte, Throwable throwable) {
		super(String.format("command 0x%02X not supported", commandByte), throwable);
		this.commandByte = commandByte;
	}

	public byte getCommandByte() {
		return commandByte;
	}
}
