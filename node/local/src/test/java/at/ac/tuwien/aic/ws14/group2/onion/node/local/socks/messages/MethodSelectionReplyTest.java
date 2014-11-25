package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.MessageParsingException;
import org.junit.Test;

import java.nio.BufferUnderflowException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MethodSelectionReplyTest {

	private static final byte[] SAMPLE_1 = new byte[]{
			(byte) 0x05, // version
			(byte) 0x00 // no auth
	};
	private static final byte[] SAMPLE_2 = new byte[]{
			(byte) 0x05, // version
			(byte) 0xFF // no acceptable methods
	};
	private static final byte[] SAMPLE_WRONG_VER = new byte[]{
			(byte) 0x04, // version
			(byte) 0x00 // no auth
	};
	private static final byte[] SAMPLE_WRONG_LENGTH = new byte[]{
			(byte) 0x05, // version
	};

	@Test
	public void testFromByteArrayShouldPass1() throws Exception {
		MethodSelectionReply reply = MethodSelectionReply.fromByteArray(SAMPLE_1);

		assertEquals(reply.getMethod(), Method.NO_AUTHENTICATION_REQUIRED);
	}

	@Test
	public void testFromByteArrayShouldPass2() throws Exception {
		MethodSelectionReply reply = MethodSelectionReply.fromByteArray(SAMPLE_2);

		assertEquals(reply.getMethod(), Method.NO_ACCEPTABLE_METHODS);
	}

	@Test(expected = MessageParsingException.class)
	public void testFromByteArrayShouldFail1() throws Exception {
		MethodSelectionReply.fromByteArray(SAMPLE_WRONG_VER);
	}

	@Test(expected = BufferUnderflowException.class)
	public void testFromByteArrayShouldFail2() throws Exception {
		MethodSelectionReply.fromByteArray(SAMPLE_WRONG_LENGTH);
	}

	@Test
	public void testToByteArrayShouldPass1() throws Exception {
		MethodSelectionReply reply = new MethodSelectionReply(Method.NO_AUTHENTICATION_REQUIRED);
		assertArrayEquals(SAMPLE_1, reply.toByteArray());
	}

	@Test
	public void testToByteArrayShouldPass2() throws Exception {
		MethodSelectionReply reply = new MethodSelectionReply(Method.NO_ACCEPTABLE_METHODS);
		assertArrayEquals(SAMPLE_2, reply.toByteArray());
	}
}