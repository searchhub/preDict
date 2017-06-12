package de.cxp.predict.customizing;

import java.util.List;

import de.cxp.predict.PreDict.AccuracyLevel;
import de.cxp.predict.api.PreDictSettings;
import de.cxp.predict.api.SuggestItem;

public interface PreDictCustomizing {

	String cleanIndexWord(String word);

	String cleanSearchWord(String searchWord);

	PreDictSettings getSettings();

	double getReplacementDistance(char a, char b);

	double adjustDistance(String searchWord, String candidate, double distance);

	/**
	 * Adds the ability to adjust the distance calculated with the
	 * edit distance logic (cxp+damerau+levenshtein), that has no access to the
	 * common prefix and suffix of these words.
	 * 
	 * @param searchWord
	 * @param suggestion
	 * @param distance
	 * @param prefixLength
	 * @param suffixLength
	 * @return
	 */
	double adjustDetailedDistance(String searchWord, String suggestion, double distance, int prefixLength,
			int suffixLength);

	/**
	 * Can be used to filter and order the final result, which is already
	 * truncated according to {@link AccuracyLevel} and sorted by
	 * {@link SuggestItem.distance} and {@link SuggestItem.count}.
	 * @param searchWord 
	 * 
	 * @param result
	 * @return
	 */
	List<SuggestItem> adjustFinalResult(String searchWord, List<SuggestItem> result);

}
