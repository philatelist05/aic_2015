package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.ParseMessageException;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
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

	public static SocksAddress fromByteArray(byte[] data) throws ParseMessageException, BufferUnderflowException {
		final int MIN_LENGTH = 1 /* ATYP */ + 2 /* PORT */;

		Objects.requireNonNull(data);
		if (data.length < MIN_LENGTH)
			throw new IllegalArgumentException("data must be at least " + MIN_LENGTH + " bytes long");

		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(SocksMessage.NETWORK_BYTE_ORDER);

		AddressType addressType = null;
		try {
			addressType = AddressType.fromByte(bb.get());
		} catch (IllegalArgumentException e) {
			throw new ParseMessageException(e);
		}

		int port;
		int length;
		switch (addressType) {
			case DOMAINNAME:
				length = Byte.toUnsignedInt(bb.get());

				if (data.length < MIN_LENGTH + 1 /* address length field */ + length)
					throw new IllegalArgumentException("data array too short");

				byte[] hostNameBytes = new byte[length];
				bb.get(hostNameBytes);
				String hostName = new String(hostNameBytes, SocksMessage.CHARSET);

				port = Short.toUnsignedInt(bb.getShort());

				return new SocksAddress(hostName, port);
			case IP_V4_ADDRESS:
			case IP_V6_ADDRESS:
				length = AddressType.IP_V4_ADDRESS.equals(addressType) ? SocksMessage.IPV4_OCTETS : SocksMessage.IPV6_OCTETS;
				byte[] addressBytes = new byte[length];
				bb.get(addressBytes);

				InetAddress address;
				try {
					address = InetAddress.getByAddress(addressBytes);
				} catch (UnknownHostException e) {
					throw new ParseMessageException(e);
				}

				port = Short.toUnsignedInt(bb.getShort());

				return new SocksAddress(address, port);
			default:
				throw new IllegalStateException("address type is in an illegal state");
		}
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
		return hostName.getBytes(SocksMessage.CHARSET);
	}

	public byte[] toByteArray() throws BufferOverflowException {
		ByteBuffer bb = null;

		int length = 1 /* ATYP */ + 2 /* PORT */;

		switch (addressType) {
			case DOMAINNAME:
				byte[] hostNameBytes = getHostNameBytes();
				length += hostNameBytes.length;

				bb = ByteBuffer.allocate(length);
				bb.order(SocksMessage.NETWORK_BYTE_ORDER);

				bb.put(addressType.getValue());

				bb.put((byte) hostNameBytes.length);

				bb.put(hostNameBytes);

				break;
			case IP_V4_ADDRESS:
			case IP_V6_ADDRESS:
				byte[] addressBytes = address.getAddress();
				length += addressBytes.length;

				if (addressBytes.length != SocksMessage.IPV4_OCTETS && addressBytes.length != SocksMessage.IPV6_OCTETS)
					throw new AssertionError();

				bb = ByteBuffer.allocate(length);
				bb.order(SocksMessage.NETWORK_BYTE_ORDER);

				bb.put(addressType.getValue());

				bb.put(addressBytes);

				break;
			default:
				throw new IllegalStateException("address type is in an illegal state");
		}

		bb.putShort((short) port);

		return bb.array();
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
