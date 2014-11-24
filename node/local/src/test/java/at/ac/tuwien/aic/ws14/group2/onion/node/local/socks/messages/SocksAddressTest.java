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

	public static final byte[] SAMPLE_WRONG_LENGTH = new byte[]{
			(byte) 0x01, // IPv4
			(byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01 // 192.168.0.1
	};
	public static final byte[] SAMPLE_WRONG_TYPE = new byte[]{
			(byte) 0x0F, // Nothing
			(byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01, // 192.168.0.1
			(byte) 0x00, (byte) 0x50 // port  80
	};
	public static final byte[] SAMPLE_IPV6_WRONG_LENGTH = new byte[]{
			(byte) 0x03, // domain name
			(byte) 0x10, // length 16 -> should be 11
			(byte) 0x65, (byte) 0x78, (byte) 0x61, (byte) 0x6d, (byte) 0x70, (byte) 0x6c,
			(byte) 0x65, (byte) 0x2e, (byte) 0x63, (byte) 0x6f, (byte) 0x6d, // example.com
			(byte) 0x1F, (byte) 0x90 // port  8080
	};
	private static final byte[] SAMPLE_IPV4 = new byte[]{
			(byte) 0x01, // IPv4
			(byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01, // 192.168.0.1
			(byte) 0x00, (byte) 0x50 // port  80
	};
	private static final byte[] SAMPLE_IPV6 = new byte[]{
			(byte) 0x04, // IPv6
			(byte) 0x00, (byte) 0x11, (byte) 0x22, (byte) 0x33,
			(byte) 0x44, (byte) 0x55, (byte) 0x66, (byte) 0x77,
			(byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB,
			(byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF, // 11:2233:4455:6677:8899:aabb:ccdd:eeff
			(byte) 0x23, (byte) 0x82 // Port  9090
	};
	private static final byte[] SAMPLE_DOMAIN_NAME = new byte[]{
			(byte) 0x03, // domain name
			(byte) 0x0B, // length 11
			(byte) 0x65, (byte) 0x78, (byte) 0x61, (byte) 0x6d, (byte) 0x70, (byte) 0x6c,
			(byte) 0x65, (byte) 0x2e, (byte) 0x63, (byte) 0x6f, (byte) 0x6d, // example.com
			(byte) 0x1F, (byte) 0x90 // port  8080
	};

	@Test
	public void testFromByteArrayIp4ShouldPass() throws Exception {
		SocksAddress address = SocksAddress.fromByteArray(SAMPLE_IPV4);

		assertEquals(address.getAddressType(), AddressType.IP_V4_ADDRESS);
		assertEquals(address.getAddress().getHostAddress(), "192.168.0.1");
		assertEquals(address.getPort(), 80);
	}

	@Test
	public void testFromByteArrayIp6ShouldPass() throws Exception {
		SocksAddress address = SocksAddress.fromByteArray(SAMPLE_IPV6);

		assertEquals(address.getAddressType(), AddressType.IP_V6_ADDRESS);
		assertEquals(address.getAddress().getHostAddress(), "11:2233:4455:6677:8899:aabb:ccdd:eeff");
		assertEquals(address.getPort(), 9090);
	}

	@Test
	public void testFromByteArrayDomainNameShouldPass() throws Exception {
		SocksAddress address = SocksAddress.fromByteArray(SAMPLE_DOMAIN_NAME);

		assertEquals(address.getAddressType(), AddressType.DOMAINNAME);
		assertEquals(address.getHostName(), "example.com");
		assertEquals(address.getPort(), 8080);
	}

	@Test(expected = BufferUnderflowException.class)
	public void testFromByteArrayShouldThrowBufferUnderflowException() throws Exception {
		SocksAddress.fromByteArray(SAMPLE_IPV6_WRONG_LENGTH);
	}

	@Test(expected = BufferUnderflowException.class)
	public void testFromByteArrayShouldThrowBufferUnderflowException2() throws Exception {
		SocksAddress.fromByteArray(SAMPLE_WRONG_LENGTH);
	}

	@Test(expected = AddressTypeNotSupportedException.class)
	public void testFromByteArrayShouldThrowAddressTypeNotSupportedException() throws Exception {
		SocksAddress.fromByteArray(SAMPLE_WRONG_TYPE);
	}

	@Test
	public void testFromByteArrayWithByteBuffer() throws Exception {
		ByteBuffer bb = ByteBuffer.wrap(SAMPLE_IPV6);
		bb.order(ByteOrder.LITTLE_ENDIAN);

		SocksAddress address = SocksAddress.fromByteArray(bb);

		assertEquals(address.getAddressType(), AddressType.IP_V6_ADDRESS);
		assertEquals(address.getAddress().getHostAddress(), "11:2233:4455:6677:8899:aabb:ccdd:eeff");
		assertEquals(address.getPort(), 9090);
	}

	@Test
	public void testToByteArrayIp4ShouldPass() throws Exception {
		SocksAddress input = new SocksAddress(InetAddress.getByName("192.168.0.1"), 80);
		byte[] actual = input.toByteArray();

		assertArrayEquals(SAMPLE_IPV4, actual);
	}

	@Test
	public void testToByteArrayDomainNameShouldPass() throws Exception {
		SocksAddress input = new SocksAddress("example.com", 8080);
		byte[] actual = input.toByteArray();

		assertArrayEquals(SAMPLE_DOMAIN_NAME, actual);
	}

	@Test
	public void testToByteArrayIp6ShouldPass() throws Exception {
		SocksAddress input = new SocksAddress(InetAddress.getByName("11:2233:4455:6677:8899:aabb:ccdd:eeff"), 9090);
		byte[] actual = input.toByteArray();

		assertArrayEquals(SAMPLE_IPV6, actual);
	}
}