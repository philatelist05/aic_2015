package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.AddressTypeNotSupportedException;
import org.junit.Test;

import java.net.InetAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SocksAddressTest {

	private static final byte[] IPV4_SAMPLE = new byte[]{
			(byte) 0x01, // IPv4
			(byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01, // 192.168.0.1
			(byte) 0x00, (byte) 0x50 // port  80
	};

	private static final byte[] IPV6_SAMPLE = new byte[]{
			(byte) 0x04, // IPv6
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01, // ::ffff:192.168.0.1
			(byte) 0x23, (byte) 0x82 // Port  9090
	};

	private static final byte[] DOMAIN_NAME_SAMPLE = new byte[]{
			(byte) 0x03, // domain name
			(byte) 0x0B, // length 11
			(byte) 0x65, (byte) 0x78, (byte) 0x61, (byte) 0x6d, (byte) 0x70, (byte) 0x6c,
			(byte) 0x65, (byte) 0x2e, (byte) 0x63, (byte) 0x6f, (byte) 0x6d, // example.com
			(byte) 0x1F, (byte) 0x90 // port  8080
	};

	@Test
	public void testFromByteArrayIp4ShouldPass() throws Exception {
		SocksAddress address = SocksAddress.fromByteArray(IPV4_SAMPLE);

		assertEquals(address.getAddressType(), AddressType.IP_V4_ADDRESS);
		assertEquals(address.getAddress().getHostAddress(), "192.168.0.1");
		assertEquals(address.getPort(), 80);
	}

	@Test
	public void testFromByteArrayIp6ShouldPass() throws Exception {
		SocksAddress address = SocksAddress.fromByteArray(IPV6_SAMPLE);

		assertEquals(address.getAddressType(), AddressType.IP_V6_ADDRESS);
		assertEquals(address.getAddress().getHostAddress(), "0:0:0:0:ffff:ffff:c0a8:1");
		assertEquals(address.getPort(), 9090);
	}

	@Test
	public void testFromByteArrayDomainNameShouldPass() throws Exception {
		SocksAddress address = SocksAddress.fromByteArray(DOMAIN_NAME_SAMPLE);

		assertEquals(address.getAddressType(), AddressType.DOMAINNAME);
		assertEquals(address.getHostName(), "example.com");
		assertEquals(address.getPort(), 8080);
	}

	@Test(expected = BufferUnderflowException.class)
	public void testFromByteArrayShouldThrowBufferUnderflowException() throws Exception {
		byte[] input = new byte[]{
				(byte) 0x03, // domain name
				(byte) 0x10, // length 16 -> should be 11
				(byte) 0x65, (byte) 0x78, (byte) 0x61, (byte) 0x6d, (byte) 0x70, (byte) 0x6c,
				(byte) 0x65, (byte) 0x2e, (byte) 0x63, (byte) 0x6f, (byte) 0x6d, // example.com
				(byte) 0x1F, (byte) 0x90 // port  8080
		};
		SocksAddress.fromByteArray(input);
	}

	@Test(expected = BufferUnderflowException.class)
	public void testFromByteArrayShouldThrowBufferUnderflowException2() throws Exception {
		byte[] input = new byte[]{
				(byte) 0x01, // IPv4
				(byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01 // 192.168.0.1
		};
		SocksAddress.fromByteArray(input);
	}

	@Test(expected = AddressTypeNotSupportedException.class)
	public void testFromByteArrayShouldThrowAddressTypeNotSupportedException() throws Exception {
		byte[] input = new byte[]{
				(byte) 0x0F, // Nothing
				(byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01, // 192.168.0.1
				(byte) 0x00, (byte) 0x50 // port  80
		};
		SocksAddress.fromByteArray(input);
	}

	@Test
	public void testFromByteArrayWithByteBuffer() throws Exception {
		ByteBuffer bb = ByteBuffer.wrap(IPV6_SAMPLE);
		bb.order(ByteOrder.LITTLE_ENDIAN);

		SocksAddress address = SocksAddress.fromByteArray(bb);

		assertEquals(address.getAddressType(), AddressType.IP_V6_ADDRESS);
		assertEquals(address.getAddress().getHostAddress(), "0:0:0:0:ffff:ffff:c0a8:1");
		assertEquals(address.getPort(), 9090);
	}

	@Test
	public void testToByteArrayIp4ShouldPass() throws Exception {
		SocksAddress input = new SocksAddress(InetAddress.getByAddress(new byte[]{(byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01}), 80);
		byte[] actual = input.toByteArray();

		assertArrayEquals(IPV4_SAMPLE, actual);
	}

	@Test
	public void testToByteArrayDomainNameShouldPass() throws Exception {
		SocksAddress input = new SocksAddress("example.com", 8080);
		byte[] actual = input.toByteArray();

		assertArrayEquals(DOMAIN_NAME_SAMPLE, actual);
	}

	@Test
	public void testToByteArrayIp6ShouldPass() throws Exception {
		SocksAddress input = new SocksAddress(InetAddress.getByAddress(new byte[]{
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01
		}), 9090);
		byte[] actual = input.toByteArray();

		assertArrayEquals(IPV6_SAMPLE, actual);
	}
}