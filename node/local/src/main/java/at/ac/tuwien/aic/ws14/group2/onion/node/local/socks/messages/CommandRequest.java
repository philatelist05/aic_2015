package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import java.util.Objects;

/**
 * Created by klaus on 11/11/14.
 */
public class CommandRequest extends SocksMessage {
	private final Command command;
	private final SocksAddress destination;

	public CommandRequest(Command command, SocksAddress destination) {
		this.command = Objects.requireNonNull(command);
		this.destination = Objects.requireNonNull(destination);
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CommandRequest that = (CommandRequest) o;

		if (command != that.command) return false;
		if (destination != null ? !destination.equals(that.destination) : that.destination != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = command != null ? command.hashCode() : 0;
		result = 31 * result + (destination != null ? destination.hashCode() : 0);
		return result;
	}
}
