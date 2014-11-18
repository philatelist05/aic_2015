package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by klaus on 11/12/14.
 */
public class SocksAddress {
	private final AddressType addressType;
	private final InetAddress address;
	private final String hostName;
	private final int port;

	public SocksAddress(InetAddress address, int port) {
		if (port > 0xFFFF || port < 0)
			throw new IllegalArgumentException("port must not be greater than " + 0xFFFF + " or less than 0");

		this.address = Objects.requireNonNull(address);
		this.port = port;
		this.hostName = null;

		if (address instanceof Inet4Address)
			this.addressType = AddressType.IP_V4_ADDRESS;
		else if (address instanceof Inet6Address)
			this.addressType = AddressType.IP_V6_ADDRESS;
		else
			throw new IllegalArgumentException("address is neither a IPv4 address nor a IPv6 address");
	}

	public SocksAddress(String hostName, int port) {
		if (port > 0xFFFF || port < 0)
			throw new IllegalArgumentException("port must not be greater than " + 0xFFFF + " or less than 0");

		this.addressType = AddressType.DOMAINNAME;
		this.address = null;
		this.port = port;
		this.hostName = Objects.requireNonNull(hostName);

		if (getHostNameBytes().length > 0xFF)
			throw new IllegalArgumentException("host name must not be longer than " + 0xFF + " bytes");
	}

	public static SocksAddress fromByteArray(byte[] data) {
		// TODO (KK) Implement address block parsing
		return null;
	}

	public String getHostName() {
		return hostName;
	}

	public AddressType getAddressType() {
		return addressType;
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	private byte[] getHostNameBytes() {
		return hostName.getBytes(Charset.defaultCharset());
	}

	public byte[] toByteArray() {
		byte[] ret = null;

		short i = 0;
		int length = 1 /* ATYP */ + 2 /* PORT */;

		switch (addressType) {
			case DOMAINNAME:
				byte[] hostNameBytes = getHostNameBytes();
				length += hostNameBytes.length;

				ret = new byte[length];

				ret[i++] = addressType.getValue();

				ret[i++] = (byte) hostNameBytes.length;

				for (byte hostNameByte : hostNameBytes) {
					ret[i++] = hostNameByte;
				}

				break;
			case IP_V4_ADDRESS:
			case IP_V6_ADDRESS:
				byte[] addressBytes = address.getAddress();
				length += addressBytes.length;

				if (addressBytes.length != 4 && addressBytes.length != 16)
					throw new AssertionError();

				ret = new byte[length];

				ret[i++] = addressType.getValue();

				for (byte addressByte : addressBytes) {
					ret[i++] = addressByte;
				}

				break;
			default:
				throw new IllegalStateException("address type is in an illegal state");
		}

		ret[i++] = (byte) (port >>> 8);
		ret[i++] = (byte) port;

		return ret;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SocksAddress that = (SocksAddress) o;

		if (port != that.port) return false;
		if (address != null ? !address.equals(that.address) : that.address != null) return false;
		if (addressType != that.addressType) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = addressType != null ? addressType.hashCode() : 0;
		result = 31 * result + (address != null ? address.hashCode() : 0);
		result = 31 * result + port;
		return result;
	}
}
