package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.AddressTypeNotSupportedException;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Created by klaus on 11/12/14.
 */
public class SocksAddress {
	public static final int MAX_PORT = 0xFFFF;
	public static final int MAX_HOSTNAME_LENGTH = 0xFF;
	public static final int MIN_PORT = 1;
	private final AddressType addressType;
	private final InetAddress address;
	private final String hostName;
	private final int port;

	public SocksAddress(InetAddress address, int port) {
		if (port > MAX_PORT || port < MIN_PORT)
			throw new IllegalArgumentException("port must not be greater than " + MAX_PORT + " or less than 0");

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
		if (port > MAX_PORT || port < MIN_PORT)
			throw new IllegalArgumentException("port must not be greater than " + MAX_PORT + " or less or equal 0");

		this.addressType = AddressType.DOMAINNAME;
		this.address = null;
		this.port = port;
		this.hostName = Objects.requireNonNull(hostName);

		if (getHostNameBytes().length > MAX_HOSTNAME_LENGTH)
			throw new IllegalArgumentException("host name must not be longer than " + MAX_HOSTNAME_LENGTH + " bytes");
	}

	public SocksAddress(InetSocketAddress inetSocketAddress) {
		this(inetSocketAddress.getAddress(), inetSocketAddress.getPort());
	}



	/**
	 * Parses a byte array to a new instance of this class.
	 *
	 * @return a new instance of this class
	 * @throws EOFException                     if the input provided is shorter than the expected length
	 * @throws AddressTypeNotSupportedException if an invalid address type byte was provided
	 */
	public static SocksAddress fromByteArray(byte[] data) throws EOFException, AddressTypeNotSupportedException {
		Objects.requireNonNull(data);

		try {
			return fromInputStream(new DataInputStream(new ByteArrayInputStream(data)));
		} catch (IOException e) {
			if (e instanceof EOFException)
				throw (EOFException) e;
			// Should never be the case since we are reading from an byte array
			throw new RuntimeException();
		}
	}

	/**
	 * Parses the bytes of the byte buffer starting at the current position and increments the position by the number of
	 * bytes read from the buffer.
	 *
	 * @return a new instance of this class
	 * @throws EOFException                     if the input provided is shorter than the expected length
	 * @throws AddressTypeNotSupportedException if an invalid address type byte was provided
	 */
	public static SocksAddress fromInputStream(DataInput input) throws IOException, AddressTypeNotSupportedException {
		Objects.requireNonNull(input);

		AddressType addressType;
		byte addressTypeByte = input.readByte();
		try {
			addressType = AddressType.fromByte(addressTypeByte);
		} catch (IllegalArgumentException e) {
			throw new AddressTypeNotSupportedException(addressTypeByte, e);
		}

		int port;
		int length;
		switch (addressType) {
			case DOMAINNAME:
				length = input.readUnsignedByte();

				byte[] hostNameBytes = new byte[length];
				input.readFully(hostNameBytes);
				String hostName = new String(hostNameBytes, SocksMessage.CHARSET);

				port = input.readUnsignedShort();

				return new SocksAddress(hostName, port);
			case IP_V4_ADDRESS:
			case IP_V6_ADDRESS:
				length = AddressType.IP_V4_ADDRESS.equals(addressType) ? SocksMessage.IPV4_OCTETS : SocksMessage.IPV6_OCTETS;
				byte[] addressBytes = new byte[length];
				input.readFully(addressBytes);

				InetAddress address = null;
				try {
					address = InetAddress.getByAddress(addressBytes);
				} catch (UnknownHostException ignored) {
					// since we specify the length, this exception can not occur
				}

				port = input.readUnsignedShort();

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

	public byte[] toByteArray() {
		ByteBuffer bb;

		int length = 1 /* ATYP */ + 2 /* PORT */;

		switch (addressType) {
			case DOMAINNAME:
				byte[] hostNameBytes = getHostNameBytes();
				length += 1 /* ADDR LEN */ + hostNameBytes.length;

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
		int result = addressType != null ? addressType.hashCode() : MIN_PORT;
		result = 31 * result + (address != null ? address.hashCode() : MIN_PORT);
		result = 31 * result + port;
		return result;
	}
}
