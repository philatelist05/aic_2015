package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

/**
 * Created by klaus on 11/11/14.
 */
public abstract class SocksMessage {
	protected final byte version = 0x05;
	protected final byte reservedByte = 0x00;

	public abstract byte[] toByteArray();

	public byte getVersion() {
		return version;
	}
}
