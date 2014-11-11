package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import java.net.InetAddress;

/**
 * Created by klaus on 11/12/14.
 */
public class SocksAddress {
	private final AddressType addressType;
	private final InetAddress address;
	private final short port;

	public SocksAddress(AddressType addressType, InetAddress address, short port) {
		this.addressType = addressType;
		this.address = address;
		this.port = port;
	}

	public static SocksAddress fromByteArray(byte[] data) {
		// TODO (KK) Implement address block parsing
		return null;
	}

	public AddressType getAddressType() {
		return addressType;
	}

	public InetAddress getAddress() {
		return address;
	}

	public short getPort() {
		return port;
	}

	public byte[] toByteArray() {
		// TODO (KK) Implement address block serialization
		return null;
	}
}
