package at.ac.tuwien.aic.ws14.group2.onion.target;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

public class QuoteStorage {

	private final List<String> quoteBuffer;

	public QuoteStorage(InputStream stream) {
		quoteBuffer = new ArrayList<>();
		fillQuoteBuffer(stream);
	}

	private void fillQuoteBuffer(InputStream stream) {
		Scanner scanner = new Scanner(stream);
		scanner.useDelimiter(Pattern.compile("^\\s*$", Pattern.MULTILINE));
		while (scanner.hasNext()) {
			quoteBuffer.add(scanner.next().trim());
		}
	}

	public String getQuote(int index) {
		return quoteBuffer.get(index);
	}

	public String getRandomQuote() {
		Random random = new Random(System.currentTimeMillis());
		return getQuote(Math.abs(random.nextInt() % quoteBuffer.size()));
	}

	public int getNumberOfQuotes() {
		return quoteBuffer.size();
	}

	public boolean containsQuote(String quote) {
		return quoteBuffer.contains(quote);
	}
}
