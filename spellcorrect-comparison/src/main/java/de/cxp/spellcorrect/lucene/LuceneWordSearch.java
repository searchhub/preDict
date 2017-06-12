package de.cxp.spellcorrect.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.DirectSpellChecker;
import org.apache.lucene.search.spell.SuggestMode;
import org.apache.lucene.search.spell.SuggestWord;
import org.apache.lucene.store.RAMDirectory;

import de.cxp.spellcorrect.ResourceBackedWordSearch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LuceneWordSearch implements ResourceBackedWordSearch, AutoCloseable {

	private static final String WORD_FIELD = "word";
	private final IndexWriter writer;
	private final RAMDirectory directory;

	private IndexSearcher searcher;
	private DirectoryReader reader;
	private DirectSpellChecker spellChecker;

	public LuceneWordSearch() {
		directory = new RAMDirectory();
		IndexWriterConfig writerConfig = new IndexWriterConfig();
		try {
			writer = new IndexWriter(directory, writerConfig);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean indexWord(String word) {
		Document doc = new Document();
		doc.add(new TextField(WORD_FIELD, word, Store.YES));
		try {
			writer.addDocument(doc);
		} catch (IOException e) {
			log.error("failed indexing word '" + word + "'", e);
			return false;
		}
		return true;
	}

	@Override
	public void indexingDone() {
		try {
			spellChecker = new DirectSpellChecker();
			spellChecker.setMaxEdits(2);
			spellChecker.setAccuracy(0.1f);
			spellChecker.setMinPrefix(0);
			reader = DirectoryReader.open(writer);

			writer.close();
			searcher = new IndexSearcher(DirectoryReader.open(directory));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<String> findSimilarWords(String searchQuery) {
		List<String> foundWords = new ArrayList<>();
		try {
			// not much faster but worse results 
			//foundWords = getUsingSpellcheck(searchQuery);
			if (foundWords.isEmpty()) {
				foundWords = getUsingFuzzySearch(searchQuery);
			}
		} catch (IOException e) {
			log.error("failed while searching for '" + searchQuery + "'", e);
		}

		return foundWords;
	}

	private List<String> getUsingSpellcheck(String searchQuery) throws IOException {
		SuggestWord[] suggestions = spellChecker.suggestSimilar(new Term(WORD_FIELD, searchQuery), 2, reader, SuggestMode.SUGGEST_ALWAYS);
		List<String> result = new ArrayList<>();
		for(SuggestWord suggestion : suggestions) {
			result.add(suggestion.string);
		}
		return result;
	}

	private List<String> getUsingFuzzySearch(String searchQuery) throws IOException {
		Query query = new FuzzyQuery(new Term(WORD_FIELD, searchQuery), 2, 0);
		List<String> foundWords = new ArrayList<>();
		TopDocs rs = searcher.search(query, 2);
		for (ScoreDoc docRef : rs.scoreDocs) {
			Document docHit = searcher.doc(docRef.doc);
			foundWords.add(docHit.get(WORD_FIELD));
		}
		return foundWords;
	}

	@Override
	public void close() throws Exception {
		reader.close();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
