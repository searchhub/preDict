package de.cxp.spellcorrect;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.time.StopWatch;

import de.cxp.spellcorrect.lucene.LuceneWordSearch;
import de.cxp.spellcorrect.predict.PreDictFactory;
import de.cxp.spellcorrect.symspell.SymSpellWordSearch;

public class WordSearchComparison {

	private static String dataFile = "src/main/resources/full_test.txt";

	private static boolean printFailures = true;
	private static boolean acceptSecondHitAsSuccess = false;

	public static void main(String[] args) throws IOException {
		WordSearch[] wordCorrectImplementations = {
				new SymSpellWordSearch(""),
				PreDictFactory.getCommunityEdition(),
				
				// works only if the dependency is on the classpath
				// PreDictFactory.getEnterpriseEdition(AccuracyLevel.maximum),
				
				new LuceneWordSearch(),

				// This impl. is worse then the Lucene one, but it should be
				// comparable, since there's also Lucene inside.
				// Maybe it's bad, because not optimally implemented.
				// new ElasticsearchWordSearch()
		};

		for (WordSearch wordCorrect : wordCorrectImplementations) {
			String name = wordCorrect.toString();
			System.out.println(name);
			System.out.println("----------------------------------------------");

			run(wordCorrect);

			System.out.println();
			System.out.println();
		}
	}

	public static void run(WordSearch wordCorrect) throws IOException {

		CSVParser parser = CSVParser.parse(new File(dataFile), Charset.forName("UTF-8"), CSVFormat.DEFAULT
				.withDelimiter(':'));

		Map<String, String> tpCandidates = new HashMap<>();
		Map<String, String> fpCandidates = new HashMap<>();

		// index
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		int indexCount = 0;
		Iterator<CSVRecord> csvIterator = parser.iterator();
		while (csvIterator.hasNext()) {
			// 0 = correct word
			// 1 = true if this is a desired match,
			// false if this is a false-positive match
			// 2 = comma separated list of similar word
			CSVRecord csvRecord = csvIterator.next();
			Boolean match = Boolean.valueOf(csvRecord.get(1));
			if (match) {
				appendToList(tpCandidates, csvRecord);
			} else {
				if (csvRecord.get(1).equals(csvRecord.get(0))) {
					System.out.println("WRONG: " + csvRecord.get(1) + "," + csvRecord.get(0) + ",false");
				}
				appendToList(fpCandidates, csvRecord);
			}

			wordCorrect.indexWord(csvRecord.get(0));
			indexCount++;
		}

		// indexing done
		if (wordCorrect instanceof ResourceBackedWordSearch) {
			((ResourceBackedWordSearch) wordCorrect).indexingDone();
		}
		stopWatch.stop();
		long indexTime = stopWatch.getTime();

		stopWatch.reset();
		stopWatch.start();

		// for each spellTestSetEntry do all searches
		int success = 0;
		int fail = 0;
		int truePositives = 0;
		int trueNegatives = 0;
		int falsePositives = 0;
		int falseNegatives = 0;
		int count = 0;

		for (Entry<String, String> candidate : tpCandidates.entrySet()) {
			List<String> results = wordCorrect.findSimilarWords(candidate.getKey());

			// first or second match count as success
			if (isMatch(candidate, results)) {
				success++;
				truePositives++;
			} else {
				if (printFailures) {
					System.out.println(count + ": '" + candidate.getValue() + "' not found by search for " + candidate
							.getKey());
					if (results.size() > 0) System.out.println("found '" + results.get(0)
							+ (results.size() > 1 ? "' and '" + results.get(1) : "")
							+ "' instead");
					System.out.println();
				}
				fail++;
				falseNegatives++;
			}
			count++;
		}

		for (Entry<String, String> candidate : fpCandidates.entrySet()) {
			List<String> results = wordCorrect.findSimilarWords(candidate.getKey());

			// first or second match count as success
			if (isMatch(candidate, results) && !candidate.getKey().equals(results.get(0))) {
				fail++;
				falsePositives++;
				if (printFailures) {
					System.out.println("false-positive: found '" + results.get(0) + "' by search for '" + candidate
							.getKey() + "'");
					if (results.size() > 1 && acceptSecondHitAsSuccess) {
						System.out.println("              + found '" + results.get(1) + "' as well'");
					}
					System.out.println();
				}
			} else {
				success++;
				trueNegatives++;
			}
			count++;
		}

		stopWatch.stop();

		System.out.println("indexed " + indexCount + " words in " + indexTime + "ms");
		System.out.println(count + " searches");
		System.out.println(stopWatch.getTime() + "ms => "
				+ String.format("%1$.3f searches/ms", ((double) count / (stopWatch.getTime()))));
		System.out.println();
		System.out.println(success + " success / accuracy => " + String.format("%.2f%%", (100.0 * success / count)));
		System.out.println(truePositives + " true-positives");
		System.out.println(trueNegatives + " true-negatives (?)");
		System.out.println();
		System.out.println(fail + " fail => " + String.format("%.2f%%", (100.0 * fail / count)));
		System.out.println(falseNegatives + " false-negatives");
		System.out.println(falsePositives + " false-positives");
		System.out.println();

		if (wordCorrect instanceof Closeable) {
			((Closeable) wordCorrect).close();
		}
	}

	private static void appendToList(Map<String, String> tpCandidates, CSVRecord csvRecord) {
		String targetWord = csvRecord.get(0);
		String[] variants = csvRecord.get(2).split(",");
		for (String variant : variants) {
			tpCandidates.put(variant, targetWord);
		}
	}

	private static boolean isMatch(Entry<String, String> candidate, List<String> results) {
		return (results.size() > 0 && results.get(0).equals(candidate.getValue()))
				|| (results.size() > 0 && results.get(0).equals(candidate.getKey()))
				|| (acceptSecondHitAsSuccess
						&& results.size() > 1
						&& results.get(1).equals(candidate.getValue()));
	}

}
