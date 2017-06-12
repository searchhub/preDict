package de.cxp.spellcorrect.symspell;

import java.util.ArrayList;
import java.util.List;

import de.cxp.spellcorrect.WordSearch;

public class SymSpellWordSearch implements WordSearch {

	private String lang;
	
	public SymSpellWordSearch(String name) {
		this.lang = name;
	}
	
	@Override
	public boolean indexWord(String word) {
		return SymSpell.CreateDictionaryEntry(word, lang);
	}

	@Override
	public List<String> findSimilarWords(String searchQuery) {
		List<String> results = new ArrayList<>();
		SymSpell.Lookup(searchQuery, lang, SymSpell.editDistanceMax).forEach(item -> results.add(item.term));;
		return results;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
