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
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	/**
	 * Function to get a hex string from a byte array. Copied from <a href="http://stackoverflow.com/a/9855338">http://stackoverflow.com/a/9855338</a>.
	 */
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public abstract byte[] toByteArray();

	public String toHexadecimalString() {
		byte[] bytes = toByteArray();
		return bytesToHex(bytes);
	}
}
