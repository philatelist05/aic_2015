package at.ac.tuwien.aic.ws14.group2.onion.target;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RestServiceTest extends JerseyTest {

	private QuoteStorage storage;

	public RestServiceTest() throws TestContainerException {
		super(new LowLevelAppDescriptor.Builder(RestService.class).
				contextPath("context").
				build());
	}

	@Before
	public void before() throws Exception{
		InputStream stream = ClassLoader.getSystemResourceAsStream(RestService.quoteFile);
		storage = new QuoteStorage(stream);
	}

	@Test
	public void testGETStatusCode() throws Exception {
		ClientResponse response = resource().path("/").get(ClientResponse.class);

		assertEquals(Response.Status.OK, Response.Status.fromStatusCode(response.getStatus()));
	}

	@Test
	public void testGETResponse() throws Exception {
		ClientResponse response = resource().path("/").get(ClientResponse.class);

		String responseString = response.getEntity(String.class);
		assertTrue(storage.containsQuote(responseString));
	}
}