package de.cxp.spellcorrect.util;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.google.common.collect.ImmutableList;

import de.cxp.spellcorrect.WordSearch;

public class TestDataProvider {

	private static final String testDataFileName = "full_test.txt";

	private final List<String> baseWords = new ArrayList<>();
	private final List<String> queries = new ArrayList<>();

	public TestDataProvider() throws IOException {
		this(testDataFileName);
	}
	
	/**
	 * expects the name of a csv resource that matches the following format:
	 * 
	 * <pre>
	 * 0 = correct word
	 * 1 = true if this is a desired match,
	 *     false if this is a false-positive match
	 * 2 = comma separated list of similar word
	 * </pre>
	 * 
	 * @param resourceName
	 * @throws IOException
	 */
	public TestDataProvider(String resourceName) throws IOException {
		URL resourceUrl = this.getClass().getClassLoader().getResource(resourceName);
		CSVParser parser = CSVParser.parse(resourceUrl, Charset.forName("UTF-8"), CSVFormat.DEFAULT.withDelimiter(':'));
		Iterator<CSVRecord> csvIterator = parser.iterator();
		while (csvIterator.hasNext()) {
			CSVRecord csvRecord = csvIterator.next();
			baseWords.add(csvRecord.get(0));
			queries.addAll(Arrays.asList(csvRecord.get(2).split(",")));
		}
	}

	public int populateWordSearch(WordSearch wordSearch) {
		for(String word : baseWords) {
			wordSearch.indexWord(word);
		}
		return baseWords.size();
	}

	public List<String> getQueries() {
		return ImmutableList.copyOf(queries);
	}
}
