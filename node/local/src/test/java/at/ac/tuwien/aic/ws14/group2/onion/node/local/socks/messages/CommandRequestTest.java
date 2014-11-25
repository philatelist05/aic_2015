package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.CommandNotSupportedException;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.MessageParsingException;
import org.junit.Test;

import java.net.InetAddress;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CommandRequestTest {

	private static final byte[] SAMPLE_CONNECT = new byte[]{
			(byte) 0x05, // version
			(byte) 0x01, // connect
			(byte) 0x00, // reserved
			(byte) 0x01, // IPv4
			(byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01, // 192.168.0.1
			(byte) 0x00, (byte) 0x50 // port  80
	};

	private static final byte[] SAMPLE_BIND = new byte[]{
			(byte) 0x05, // version
			(byte) 0x02, // bind
			(byte) 0x00, // reserved
			(byte) 0x01, // IPv4
			(byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01, // 192.168.0.1
			(byte) 0x00, (byte) 0x50 // port  80
	};

	private static final byte[] SAMPLE_WRONG_COMMAND = new byte[]{
			(byte) 0x05, // version
			(byte) 0xF0, // nothing
			(byte) 0x00, // reserved
			(byte) 0x01, // IPv4
			(byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01, // 192.168.0.1
			(byte) 0x00, (byte) 0x50 // port  80
	};

	private static final byte[] SAMPLE_WRONG_VERSION = new byte[]{
			(byte) 0x03, // version
			(byte) 0x02, // bind
			(byte) 0x00, // reserved
			(byte) 0x01, // IPv4
			(byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01, // 192.168.0.1
			(byte) 0x00, (byte) 0x50 // port  80
	};

	private static final byte[] SAMPLE_WRONG_RESERVED = new byte[]{
			(byte) 0x03, // version
			(byte) 0x02, // bind
			(byte) 0x00, // reserved
			(byte) 0x01, // IPv4
			(byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01, // 192.168.0.1
			(byte) 0x00, (byte) 0x50 // port  80
	};

	@Test
	public void testFromByteArrayShouldPass1() throws Exception {
		CommandRequest commandRequest = CommandRequest.fromByteArray(SAMPLE_CONNECT);

		assertEquals(commandRequest.getCommand(), Command.CONNECT);
		assertEquals(commandRequest.getDestination().getAddressType(), AddressType.IP_V4_ADDRESS);
		assertEquals(commandRequest.getDestination().getAddress(), InetAddress.getByName("192.168.0.1"));
		assertEquals(commandRequest.getDestination().getPort(), 80);
	}

	@Test
	public void testFromByteArrayShouldPass2() throws Exception {
		CommandRequest commandRequest = CommandRequest.fromByteArray(SAMPLE_BIND);

		assertEquals(commandRequest.getCommand(), Command.BIND);
		assertEquals(commandRequest.getDestination().getAddressType(), AddressType.IP_V4_ADDRESS);
		assertEquals(commandRequest.getDestination().getAddress(), InetAddress.getByName("192.168.0.1"));
		assertEquals(commandRequest.getDestination().getPort(), 80);
	}

	@Test(expected = CommandNotSupportedException.class)
	public void testFromByteArrayShouldFail1() throws Exception {
		CommandRequest.fromByteArray(SAMPLE_WRONG_COMMAND);
	}

	@Test(expected = MessageParsingException.class)
	public void testFromByteArrayShouldFail2() throws Exception {
		CommandRequest.fromByteArray(SAMPLE_WRONG_VERSION);
	}

	@Test(expected = MessageParsingException.class)
	public void testFromByteArrayShouldFail3() throws Exception {
		CommandRequest.fromByteArray(SAMPLE_WRONG_RESERVED);
	}

	@Test
	public void testToByteArrayShouldPass1() throws Exception {
		CommandRequest request = new CommandRequest(Command.BIND, new SocksAddress(InetAddress.getByName("192.168.0.1"), 80));
		assertArrayEquals(SAMPLE_BIND, request.toByteArray());

	}

	@Test
	public void testToByteArrayShouldPass2() throws Exception {
		CommandRequest request = new CommandRequest(Command.CONNECT, new SocksAddress(InetAddress.getByName("192.168.0.1"), 80));
		assertArrayEquals(SAMPLE_CONNECT, request.toByteArray());

	}
}