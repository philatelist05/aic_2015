package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions;

/**
 * Created by klaus on 11/18/14.
 */
public class AddressTypeNotSupportedException extends SocksException {
	public AddressTypeNotSupportedException() {
		super();
	}

	public AddressTypeNotSupportedException(String s) {
		super(s);
	}

	public AddressTypeNotSupportedException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public AddressTypeNotSupportedException(Throwable throwable) {
		super(throwable);
	}
}
