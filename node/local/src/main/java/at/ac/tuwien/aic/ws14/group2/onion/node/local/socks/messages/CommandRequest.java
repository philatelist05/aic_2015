package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

/**
 * Created by klaus on 11/11/14.
 */
public class CommandRequest extends SocksMessage {
	private final Command command;
	private final SocksAddress destination;

	public CommandRequest(Command command, SocksAddress destination) {
		this.command = command;
		this.destination = destination;
	}

	public static CommandRequest fromByteArray(byte[] data) {
		// TODO (KK) Implement command request message parsing
		return null;
	}

	public Command getCommand() {
		return command;
	}

	public SocksAddress getDestination() {
		return destination;
	}

	@Override
	public byte[] toByteArray() {
		// TODO (KK) Implement command request serialization
		return new byte[0];
	}
}
