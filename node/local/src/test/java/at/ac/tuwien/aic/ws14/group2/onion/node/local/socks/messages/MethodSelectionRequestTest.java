package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.messages;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.MessageParsingException;
import org.junit.Test;

import java.io.EOFException;

import static org.junit.Assert.assertArrayEquals;

public class MethodSelectionRequestTest {

	private static final byte[] SAMPLE_1 = new byte[]{
			(byte) 0x05, // version
			(byte) 0x01, // length 1
			(byte) 0x00 // no auth
	};
	private static final byte[] SAMPLE_2 = new byte[]{
			(byte) 0x05, // version
			(byte) 0x03, // length 3
			(byte) 0x00, // no auth
			(byte) 0x01, // gssapi
			(byte) 0x02 // username/password
	};
	private static final byte[] SAMPLE_WRONG_VER = new byte[]{
			(byte) 0x04, // version
			(byte) 0x01, // length 1
			(byte) 0x00 // no auth
	};
	private static final byte[] SAMPLE_WRONG_LENGTH = new byte[]{
			(byte) 0x05, // version
			(byte) 0x02, // length 2
			(byte) 0x00 // no auth
	};
	private static final byte[] SAMPLE_UNKNOWN_METHOD = new byte[]{
			(byte) 0x05, // version
			(byte) 0x02, // length 2
			(byte) 0x00, // no auth
			(byte) 0x88 // unknown
	};

	@Test
	public void testFromByteArrayShouldPass1() throws Exception {
		MethodSelectionRequest request = MethodSelectionRequest.fromByteArray(SAMPLE_1);

		assertArrayEquals(request.getMethods(), new Method[]{Method.NO_AUTHENTICATION_REQUIRED});
	}

	@Test
	public void testFromByteArrayShouldPass2() throws Exception {
		MethodSelectionRequest request = MethodSelectionRequest.fromByteArray(SAMPLE_2);

		assertArrayEquals(request.getMethods(), new Method[]{Method.NO_AUTHENTICATION_REQUIRED, Method.GSSAPI, Method.USERNAME_PASSWORD});
	}

	@Test
	public void testFromByteArrayShouldPassUnknownMethod() throws Exception {
		MethodSelectionRequest request = MethodSelectionRequest.fromByteArray(SAMPLE_UNKNOWN_METHOD);

		assertArrayEquals(request.getMethods(), new Method[]{Method.NO_AUTHENTICATION_REQUIRED, Method.UNKNOWN_METHOD});
	}

	@Test(expected = MessageParsingException.class)
	public void testFromByteArrayShouldFail1() throws Exception {
		MethodSelectionRequest.fromByteArray(SAMPLE_WRONG_VER);
	}

	@Test(expected = EOFException.class)
	public void testFromByteArrayShouldFail2() throws Exception {
		MethodSelectionRequest.fromByteArray(SAMPLE_WRONG_LENGTH);
	}

	@Test
	public void testToByteArrayShouldPass1() throws Exception {
		MethodSelectionRequest request = new MethodSelectionRequest(Method.NO_AUTHENTICATION_REQUIRED);
		assertArrayEquals(SAMPLE_1, request.toByteArray());
	}

	@Test
	public void testToByteArrayShouldPass2() throws Exception {
		MethodSelectionRequest request = new MethodSelectionRequest(Method.NO_AUTHENTICATION_REQUIRED, Method.GSSAPI, Method.USERNAME_PASSWORD);
		assertArrayEquals(SAMPLE_2, request.toByteArray());
	}
}