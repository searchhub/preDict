package de.cxp.spellcorrect;

import java.util.List;

/**
 * common api for a word search & correction implementation.
 * 
 * @author Rudolf Batt
 */
public interface WordSearch {

	boolean indexWord(String word);
	
	List<String> findSimilarWords(String searchQuery);

}
