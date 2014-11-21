package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions;

/**
 * Created by klaus on 11/18/14.
 */
public class AddressTypeNotSupportedException extends SocksException {
	private final byte addressTypeByte;

	public AddressTypeNotSupportedException(byte addressTypeByte) {
		super(String.format("address type 0x%02X not supported", addressTypeByte));
		this.addressTypeByte = addressTypeByte;
	}

	public AddressTypeNotSupportedException(byte addressTypeByte, Throwable throwable) {
		super(String.format("address type 0x%02X not supported", addressTypeByte), throwable);
		this.addressTypeByte = addressTypeByte;
	}

	public byte getAddressTypeByte() {
		return addressTypeByte;
	}
}
