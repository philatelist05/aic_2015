package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

/**
 * <p>
 * All messages are implemented according to RFC 1928
 * </p>
 * <p>
 * Created by klaus on 11/11/14.
 * </p>
 */
public abstract class SocksMessage {
	public final byte VERSION = 0x05;
	public final byte RESERVED_BYTE = 0x00;

	public abstract byte[] toByteArray();
}
