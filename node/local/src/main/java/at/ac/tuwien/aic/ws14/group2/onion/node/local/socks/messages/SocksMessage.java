package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * <p>
 * All messages are implemented according to RFC 1928
 * </p>
 * <p>
 * Created by klaus on 11/11/14.
 * </p>
 */
public abstract class SocksMessage {
	public static final byte VERSION = 0x05;
	public static final byte RESERVED_BYTE = 0x00;
	public static final Charset CHARSET = Charset.defaultCharset();
	public static final ByteOrder NETWORK_BYTE_ORDER = ByteOrder.BIG_ENDIAN;
	public static final int IPV4_OCTETS = 4;
	public static final int IPV6_OCTETS = 16;

	public abstract byte[] toByteArray();
}
