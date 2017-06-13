package de.cxp.predict.customizing;

import java.util.List;

import de.cxp.predict.PreDict.AccuracyLevel;
import de.cxp.predict.api.PreDictSettings;
import de.cxp.predict.api.SuggestItem;

public interface PreDictCustomizing {

	/**
	 * Modify word before it get's indexed.
	 * Returns the unmodified word by default.
	 * 
	 * @param word
	 * @return modified word
	 */
	default String cleanIndexWord(String word) {
		return word;
	};

	/**
	 * Modify word before it is used to for search.
	 * Returns the unmodified word by default.
	 * 
	 * @param searchWord the original search word
	 * @return the modified search word
	 */
	default String cleanSearchWord(String searchWord) {
		return searchWord;
	};

	PreDictSettings getSettings();

	/**
	 * Returns the distance between two characters, which is 1 by default.
	 * Can be used for a advanced keyboard distance calculation or similar.
	 * 
	 * @param char a
	 * @param char b
	 * @return
	 */
	default double getReplacementDistance(char a, char b) {
		return 1.0;
	};

	/**
	 * Adjust the final distance before adding the candidate to the result list.
	 * Returns the unchanged given distance by default.
	 * 
	 * @param searchWord the modified search word
	 * @param candidate
	 * @param distance
	 * @return
	 */
	default double adjustDistance(String searchWord, String candidate, double distance) {
		return distance;
	};

	/**
	 * Adjust the distance after it got calculated with the edit distance logic
	 * (cxp+damerau+levenshtein), that has no access to the common prefix and
	 * suffix of these words.
	 * Returns the unchanged given distance by default.
	 * 
	 * @param searchWord the modified search word
	 * @param suggestion
	 * @param distance
	 * @param prefixLength
	 * @param suffixLength
	 * @return
	 */
	default double adjustDetailedDistance(String searchWord, String suggestion, double distance, int prefixLength,
			int suffixLength) {
		return distance;
	};

	/**
	 * Can be used to filter and order the final result, which is already
	 * truncated according to {@link AccuracyLevel} and sorted by
	 * {@link SuggestItem.distance} and {@link SuggestItem.count}.
	 * 
	 * @param searchWord the modified search word
	 * @param result
	 * @return
	 */
	default List<SuggestItem> adjustFinalResult(String searchWord, List<SuggestItem> result) {
		return result;
	};

}
