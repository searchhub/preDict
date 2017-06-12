package de.cxp.spellcorrect.elasticsearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.network.NetworkModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.Suggest.Suggestion;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry.Option;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.term.TermSuggestionBuilder.SuggestMode;

import com.google.common.io.Files;

import de.cxp.spellcorrect.ResourceBackedWordSearch;

public class ElasticsearchWordSearch implements ResourceBackedWordSearch, AutoCloseable {

	private static final String INDEX_NAME = "wordcorrect";
	private static final String INDEX_TYPE = "word";
	private static final String FIELD_NAME = "input";

	private final Client client;
	private final Node node;
	private BulkRequestBuilder bulk;

	public ElasticsearchWordSearch() {
		node = newNode();
		client = node.client();
		prepareIndex();
	}

	private void prepareIndex() {
		try {
			XContentBuilder source = XContentFactory.jsonBuilder()
					.startObject()
						.startObject(INDEX_TYPE)
							.startObject("properties")
								.startObject(FIELD_NAME)
									.field("type", "text")
									.field("analyzer", "simple")
								.endObject()
							.endObject()
						.endObject()
					.endObject();
			client.admin().indices()
					.prepareCreate(INDEX_NAME)
					.addMapping(INDEX_TYPE, source)
					.setSettings(Settings.builder()
							.put("number_of_shards", 1)
							.put("number_of_replicas", 0))
					.get();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean indexWord(String word) {
		if (bulk == null) {
			bulk = client.prepareBulk();
		}
		try {
			bulk.add(client.prepareIndex(INDEX_NAME, INDEX_TYPE)
					.setSource(XContentFactory.jsonBuilder()
							.startObject()
							.field(FIELD_NAME, word)
							.endObject()));
		} catch (IOException e) {
			// should never happen
			throw new RuntimeException(e);
		}
		return true;
	}

	@Override
	public void indexingDone() {
		bulk.get();
	}

	@Override
	public List<String> findSimilarWords(String searchQuery) {
		SearchResponse result = client.prepareSearch(INDEX_NAME)
				.suggest(new SuggestBuilder()
						.addSuggestion(FIELD_NAME,
								SuggestBuilders
										.termSuggestion(FIELD_NAME)
										.maxEdits(2)
										.suggestMode(SuggestMode.ALWAYS)
										.size(1)
										.text(searchQuery)))
				.get();

		Suggest suggest = result.getSuggest();
		List<String> similarWords = new ArrayList<>();
		if (suggest != null) {
			Suggestion<? extends Entry<? extends Option>> suggestions = suggest.getSuggestion(FIELD_NAME);
			for (Entry<? extends Option> options : suggestions.getEntries()) {
				for (Option option : options.getOptions()) {
					similarWords.add(option.getText().string());
				}
			}
		}
		
		return similarWords;
	}

	private Node newNode() {
		File tempDir = Files.createTempDir();
		Settings settings = Settings.builder()
				.put(Environment.PATH_HOME_SETTING.getKey(), tempDir)
				.put("node.name", "node_s_0")
				.put(NetworkModule.HTTP_ENABLED.getKey(), false)
				.put("transport.type", "local")
				.put(Node.NODE_DATA_SETTING.getKey(), true)
				.build();
		Node build = new Node(settings);
		try {
			build.start();
		} catch (NodeValidationException e) {
			throw new RuntimeException(e);
		}
		return build;
	}

	@Override
	public void close() throws Exception {
		if (client != null) client.close();
		if (node != null) node.close();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
