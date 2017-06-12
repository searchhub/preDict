package de.cxp.spellcorrect.predict;

import java.util.List;

import de.cxp.predict.PreDict;
import de.cxp.spellcorrect.WordSearch;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PreDictWrapper implements WordSearch {

	private final PreDict preDict;
	
	@Override
	public boolean indexWord(String word) {
		return preDict.indexWord(word);
	}

	@Override
	public List<String> findSimilarWords(String searchQuery) {
		return preDict.findSimilarWords(searchQuery);
	}

	@Override
	public String toString() {
		return preDict.toString();
	}
}
