package at.ac.tuwien.aic.ws14.group2.onion.target;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.InputStream;

@Provider
@Path("/")
public class RestService {

	static final String quoteFile = "orbname.txt";

	@GET
	public Response getResource() {
		QuoteStorage quoteStorage = createQuoteStorage();
		String quote = quoteStorage.getRandomQuote();
		return Response.ok(quote).build();
	}

	private QuoteStorage createQuoteStorage() {
		InputStream stream = ClassLoader.getSystemResourceAsStream(quoteFile);
		if (stream == null)
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);

		return new QuoteStorage(stream);
	}
}
