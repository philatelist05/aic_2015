package at.ac.tuwien.aic.ws14.group2.onion.target;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QuoteStorageTest {

	@Test
	public void testGetNumberOfQuotesZero() throws Exception {
		String quotes = "";
		ByteArrayInputStream stream = new ByteArrayInputStream(quotes.getBytes(Charset.forName("UTF-8")));
		QuoteStorage quoteStorage = new QuoteStorage(stream);

		assertEquals(0, quoteStorage.getNumberOfQuotes());
	}

	@Test
	public void testGetNumberOfQuotesOne() throws Exception {
		String quotes = "This is a quote";
		ByteArrayInputStream stream = new ByteArrayInputStream(quotes.getBytes(Charset.forName("UTF-8")));
		QuoteStorage quoteStorage = new QuoteStorage(stream);

		assertEquals(1, quoteStorage.getNumberOfQuotes());
	}

	@Test
	public void testGetNumberOfQuotesTwo() throws Exception {
		String quotes = buildQuoteString("quote1", "quote2");
		ByteArrayInputStream stream = new ByteArrayInputStream(quotes.getBytes(Charset.forName("UTF-8")));
		QuoteStorage quoteStorage = new QuoteStorage(stream);

		assertEquals(2, quoteStorage.getNumberOfQuotes());
	}

	@Test
	public void testGetQuote() throws Exception {
		String quotes = buildQuoteString("quote1", "quote2");
		ByteArrayInputStream stream = new ByteArrayInputStream(quotes.getBytes(Charset.forName("UTF-8")));
		QuoteStorage quoteStorage = new QuoteStorage(stream);

		assertEquals("quote2", quoteStorage.getQuote(1));
	}

	@Test
	public void testContainsQuote() throws Exception {
		String quotes = buildQuoteString("quote1", "quote2", "quote3");
		ByteArrayInputStream stream = new ByteArrayInputStream(quotes.getBytes(Charset.forName("UTF-8")));
		QuoteStorage quoteStorage = new QuoteStorage(stream);

		assertTrue(quoteStorage.containsQuote("quote2"));
	}

	@Test
	public void testGetRandomQuote() throws Exception {
		String[] quoteArray = {"quote1", "quote2", "quote3"};
		String quotes = buildQuoteString(quoteArray);
		ByteArrayInputStream stream = new ByteArrayInputStream(quotes.getBytes(Charset.forName("UTF-8")));
		QuoteStorage quoteStorage = new QuoteStorage(stream);

		String randomQuote = quoteStorage.getRandomQuote();
		assertTrue(Arrays.asList(quoteArray).contains(randomQuote));
	}

	private String buildQuoteString(String... quotes) {
		String newLine = System.lineSeparator() + System.lineSeparator();
		return String.join(newLine, quotes);
	}
}