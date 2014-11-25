package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.MessageParsingException;
import org.junit.Test;

import java.net.InetAddress;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CommandReplyTest {

	private static final byte[] SAMPLE_SUCCEEDED = new byte[]{
			(byte) 0x05, // version
			(byte) 0x00, // succeeded
			(byte) 0x00, // reserved
			(byte) 0x01, // IPv4
			(byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01, // 192.168.0.1
			(byte) 0x00, (byte) 0x50 // port  80
	};

	private static final byte[] SAMPLE_GENERAL = new byte[]{
			(byte) 0x05, // version
			(byte) 0x01, // general SOCKS server failure
			(byte) 0x00, // reserved
			(byte) 0x01, // IPv4
			(byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x01, // 192.168.0.1
			(byte) 0x00, (byte) 0x50 // port  80
	};

	private static final byte[] SAMPLE_WRONG_TYPE = new byte[]{
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
		CommandReply commandReply = CommandReply.fromByteArray(SAMPLE_SUCCEEDED);

		assertEquals(commandReply.getReplyType(), ReplyType.SUCCEEDED);
		assertEquals(commandReply.getBoundAddress().getAddressType(), AddressType.IP_V4_ADDRESS);
		assertEquals(commandReply.getBoundAddress().getAddress(), InetAddress.getByName("192.168.0.1"));
		assertEquals(commandReply.getBoundAddress().getPort(), 80);
	}

	@Test
	public void testFromByteArrayShouldPass2() throws Exception {
		CommandReply commandReply = CommandReply.fromByteArray(SAMPLE_GENERAL);

		assertEquals(commandReply.getReplyType(), ReplyType.GENERAL_SERVER_FAILURE);
		assertEquals(commandReply.getBoundAddress().getAddressType(), AddressType.IP_V4_ADDRESS);
		assertEquals(commandReply.getBoundAddress().getAddress(), InetAddress.getByName("192.168.0.1"));
		assertEquals(commandReply.getBoundAddress().getPort(), 80);
	}

	@Test(expected = MessageParsingException.class)
	public void testFromByteArrayShouldFail1() throws Exception {
		CommandReply.fromByteArray(SAMPLE_WRONG_VERSION);
	}

	@Test(expected = MessageParsingException.class)
	public void testFromByteArrayShouldFail2() throws Exception {
		CommandReply.fromByteArray(SAMPLE_WRONG_RESERVED);
	}

	@Test(expected = MessageParsingException.class)
	public void testFromByteArrayShouldFail3() throws Exception {
		CommandReply.fromByteArray(SAMPLE_WRONG_TYPE);
	}

	@Test
	public void testToByteArrayShouldPass1() throws Exception {
		CommandReply request = new CommandReply(ReplyType.SUCCEEDED, new SocksAddress(InetAddress.getByName("192.168.0.1"), 80));
		assertArrayEquals(SAMPLE_SUCCEEDED, request.toByteArray());
	}

	@Test
	public void testToByteArrayShouldPass2() throws Exception {
		CommandReply request = new CommandReply(ReplyType.GENERAL_SERVER_FAILURE, new SocksAddress(InetAddress.getByName("192.168.0.1"), 80));
		assertArrayEquals(SAMPLE_GENERAL, request.toByteArray());
	}

}