package de.cxp.predict;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Ordering;

import de.cxp.predict.api.DictionaryItem;
import de.cxp.predict.api.PreDictSettings;
import de.cxp.predict.api.SuggestItem;
import de.cxp.predict.customizing.PreDictCustomizing;

/**
 * Based on a SymSpell Port
 * 
 * Original SymSpell by:
 * Copyright (C) 2015 Wolf Garbe
 * Version: 3.0
 * Author: Wolf Garbe <wolf.garbe@faroo.com>
 * Maintainer: Wolf Garbe <wolf.garbe@faroo.com>
 * URL: http://blog.faroo.com/2012/06/07/improved-edit-distance-based-spelling-correction/
 * Description: http://blog.faroo.com/2012/06/07/improved-edit-distance-based-spelling-correction/
 * 
 * Changes by:
 * Copyright (C) 2017 CXP Commerce Experts
 * Version: 0.1.0
 * Author: Andreas Wagner <andreas.wagner@commerce-experts.com>
 * Maintainer: Rudolf Batt <rudolf.batt@commerce-experts.com>
 * URL: https://github.com/searchhub/preDict
 * 
 * License:
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License, 
 * version 3.0 (LGPL-3.0) as published by the Free Software Foundation.
 * http://www.opensource.org/licenses/LGPL-3.0
 */
public class PreDict {

	private final PreDictCustomizing customizing;

	public PreDict(PreDictCustomizing customizing) {
		this.customizing = customizing;
		
		// copy the settings to ensure a immutable object
		PreDictSettings settings = customizing.getSettings();
		editDistanceMax = settings.getEditDistanceMax();
		accuracyLevel = settings.getAccuracyLevel();
		topK = settings.getTopK();
		deletionWeight = settings.getDeletionWeight();
		insertionWeight = settings.getInsertionWeight();
		transpositionWeight = settings.getTranspositionWeight();
		replaceWeight = settings.getReplaceWeight();
	}

	/**
	 * 2 = maximum recall and precision
	 * 1 = faster calculation with less accuracy
	 * 0 = similar as 1, but returns only top hit
	 */
	public static enum AccuracyLevel {
		topHit, fast, maximum;
	}

	private final AccuracyLevel accuracyLevel;
	
	private final int editDistanceMax;
	
	// limit suggestion list to topK entries
	private final int topK; 

	// Damerau function variables
	private final double deletionWeight;

	private final double insertionWeight;

	private final double replaceWeight;

	private final double transpositionWeight;

	private final Comparator<SuggestItem> distanceCountComparator = new Comparator<SuggestItem>() {
		public int compare(SuggestItem x, SuggestItem y) {
			return (2 * Double.compare(x.distance, y.distance) - Integer.compare(x.count, y.count));
		}
	};
	
	// Dictionary that contains both the original words and the deletes derived
	// from them. A term might be both word and delete from another word at the
	// same time.
	// For space reduction a item might be either of type DictionaryItem or int.
	// A DictionaryItem is used for word, word/delete, and delete with multiple
	// suggestions. Int is used for deletes with a single suggestion (the
	// majority of entries).
	private final HashMap<String, Object> dictionary = new HashMap<String, Object>();

	// List of unique words. By using the suggestions (int) as index for this
	// list they are translated into the original String.
	private final List<String> wordlist = new ArrayList<String>();

	private int maxlength = 0; // maximum dictionary term length


	public boolean indexWord(String word) {
		word = customizing.cleanIndexWord(word);
		DictionaryItem value = appendToDictionary(word);

		// edits/suggestions are created only once, no matter how often a word
		// occurs. they are created only as soon as the word occurs in the
		// corpus, even if the same term existed before in the dictionary as an
		// edit from another word.
		// a threshold might be specified, when a term occurs so frequently in
		// the corpus that it is considered a valid word for spelling correction
		if (value.count == 1) {
			indexFragments(word);
		}
		return true;
	}

	private DictionaryItem appendToDictionary(String word) {
		DictionaryItem value;
		Object dictionaryEntry = dictionary.get(word);

		// known word or fragment
		if (dictionaryEntry != null) {
			value = asDictionaryItem(dictionaryEntry);

			// replace integer entry with DictionaryItem object
			if (dictionaryEntry instanceof Integer) {
				dictionary.put(word, value);
			}

			// prevent overflow
			if (value.count < Integer.MAX_VALUE) value.count++;
		}
		// this is a new word
		else if (wordlist.size() < Integer.MAX_VALUE) {
			value = new DictionaryItem();
			value.count++;
			dictionary.put(word, value);
			if (word.length() > maxlength) maxlength = word.length();
		} else {
			throw new IllegalStateException("can not index word since wordlist reached limit of Integer.MAX_VALUE");
		}
		return value;
	}

	private void indexFragments(String word) {
		wordlist.add(word);
		int wordNr = wordlist.size() - 1;

		// create deletes aka fragements
		for (String fragment : getEdits(word, 0, new HashSet<String>())) {
			Object dictionaryEntry;
			dictionaryEntry = dictionary.get(fragment);
			if (dictionaryEntry != null) {
				// scenario where this entry already exists:
				// 1. word == deletes(anotherWord)
				// 2. deletes(word) == deletes(anotherWord)
				if (dictionaryEntry instanceof Integer) {
					DictionaryItem dictItem = asDictionaryItem(dictionaryEntry);
					dictionary.put(fragment, dictItem);
					if (wordNr != (int) dictionaryEntry)
						addLowestDistance(dictItem, word, wordNr, fragment);
				} else if (!((DictionaryItem) dictionaryEntry).suggestions.contains(wordNr))
					addLowestDistance((DictionaryItem) dictionaryEntry, word, wordNr, fragment);
			} else {
				dictionary.put(fragment, wordNr);
			}
		}
	}

	private DictionaryItem asDictionaryItem(Object entry) {
		if (entry instanceof DictionaryItem) {
			return (DictionaryItem) entry;
		} else if (entry instanceof Integer) {
			// if value is an integer, word is also a fragment from another word
			// => append fragment to suggestions
			DictionaryItem dictItem = new DictionaryItem();
			dictItem.suggestions.add((int) entry);
			return dictItem;
		} else {
			throw new IllegalStateException("unknown entry type found: " + entry.getClass().getSimpleName());
		}
	}

	// inexpensive and language independent: only deletes, no transposes +
	// replaces + inserts
	// replaces and inserts are expensive and language dependent (Chinese has
	// 70,000 Unicode Han characters)
	private HashSet<String> getEdits(String word, int editDistance, HashSet<String> deletes) {
		editDistance++;
		if (word.length() > 1) {
			for (int i = 0; i < word.length(); i++) {
				// delete ith character
				String delete = word.substring(0, i) + word.substring(i + 1);
				if (deletes.add(delete)) {
					// recursion, if maximum edit distance not yet reached
					if (editDistance < editDistanceMax) getEdits(delete, editDistance, deletes);
				}
			}
		}
		return deletes;
	}

	// save some time and space
	private void addLowestDistance(DictionaryItem item, String word, int wordNr, String fragment) {
		int indexedDistance = item.suggestions.size() > 0
				? wordlist.get(item.suggestions.get(0)).length() - fragment.length()
				: -1;
		int fragmentDistance = word.length() - fragment.length();

		// remove all existing suggestions (of higher distance) if this word has
		// a lower distance (only at recallLevel < 2)
		if ((accuracyLevel.ordinal() < 2) && (indexedDistance > fragmentDistance)) {
			item.suggestions.clear();
		}

		// if recall level is 2, add this word anyways
		// otherwise only add it if it has a similar or lower distance
		// then the indexed words
		if ((accuracyLevel.ordinal() == 2)
				|| (item.suggestions.size() == 0)
				|| (indexedDistance >= fragmentDistance)) {
			item.suggestions.add(wordNr);
		}
	}

	public List<String> findSimilarWords(String searchQuery) {
		List<SuggestItem> suggestions = lookup(searchQuery, editDistanceMax);
		
		List<String> similarWords = new ArrayList<>();
		suggestions.forEach(suggestion -> similarWords.add(suggestion.term));
		
		return similarWords;
	}

	private List<SuggestItem> lookup(String searchWord, int editDistanceMax) {
		String cleanedSearchWord = customizing.cleanSearchWord(searchWord);

		// save some time
		if (cleanedSearchWord.length() - editDistanceMax > maxlength)
			return new ArrayList<SuggestItem>();

		List<String> candidates = new ArrayList<String>();
		HashSet<String> candidatesUniq = new HashSet<String>();

		List<SuggestItem> suggestions = new ArrayList<SuggestItem>();
		HashSet<String> checkedWords = new HashSet<String>();

		Object dictionaryEntry;

		// add original term
		candidates.add(cleanedSearchWord);

		while (candidates.size() > 0) {
			String candidate = candidates.remove(0);

			nosort: {

				// if recallLevel is lower 2, save some time by early
				// termination (of candidate check)
				// if candidate distance is already higher than
				// distance of first suggestion
				if ((accuracyLevel.ordinal() < 2)
						&& (suggestions.size() > 0)
						&& (cleanedSearchWord.length() - candidate.length() > suggestions.get(0).distance))
					break nosort;

				// read candidate entry from dictionary
				dictionaryEntry = dictionary.get(candidate);
				if (dictionaryEntry != null) {
					DictionaryItem matchedDictionaryItem = asDictionaryItem(dictionaryEntry);

					// if count>0 then candidate entry is correct dictionary
					// term, not only delete item
					if ((matchedDictionaryItem.count > 0) && checkedWords.add(candidate)) {
						// add correct dictionary term term to suggestion list
						SuggestItem si = new SuggestItem();
						si.term = candidate;
						si.count = matchedDictionaryItem.count;
						si.wordFrequency = ((double) si.count / dictionary.size());
						si.distance = getMaxDistance(cleanedSearchWord, candidate);

						si.distance = customizing.adjustDistance(cleanedSearchWord, candidate, si.distance);

						if (si.distance <= editDistanceMax) {
							suggestions.add(si);
						}
						// early termination
						if ((accuracyLevel.ordinal() < 2) && (cleanedSearchWord.length() - candidate.length() == 0))
							break nosort;
					}

					// iterate through suggestions (to other correct dictionary
					// items) of delete item and add them to suggestion list
					for (int wordNr : matchedDictionaryItem.suggestions.toArray()) {
						// save some time by skipping double items early:
						// different deletes of the input term can lead to
						// the same suggestion
						String suggestion = wordlist.get(wordNr);
						if (checkedWords.add(suggestion)) {
							// Symmetric Delete Spelling Correction Magic:
							// adjust distance, if both distances>0
							// We allow simultaneous edits (deletes) of
							// editDistanceMax on both the dictionary and the
							// input term.
							// For replaces and adjacent transposes the
							// resulting edit distance stays <= editDistanceMax.
							// For inserts and deletes the resulting edit
							// distance might exceed editDistanceMax.
							// To prevent suggestions of a higher edit distance,
							// we need to calculate the resulting edit distance,
							// if there are simultaneous edits on both sides.
							// Example: (bank==bnak and bank==bink, but
							// bank!=kanb and bank!=xban and bank!=baxn for
							// editDistanceMaxe=1)
							// Two deletes on each side of a pair makes them all
							// equal, but the first two pairs have edit
							// distance=1, the others edit distance=2.
							double distance = 0;
							if (!suggestion.equals(cleanedSearchWord)) {
								// Case 1: if only deletes match the dictionary
								if (suggestion.length() == candidate.length()) {
									distance = getMaxDistance(cleanedSearchWord, candidate);
								} else if (cleanedSearchWord.length() == candidate.length()) {
									distance = getMaxDistance(suggestion, candidate);

									// Case 2: if further edits additional to
									// the deletes need to happen in order to
									// match the dictionary
								} else {
									// common prefixes and suffixes are ignored,
									// because this speeds up the
									// Damerau-levenshtein-Distance calculation
									// without changing it.
									int prefixLength = 0;
									int suffixLength = 0;

									while ((prefixLength < suggestion.length()) && (prefixLength < cleanedSearchWord.length())
											&& (suggestion
													.charAt(
															prefixLength) == cleanedSearchWord.charAt(prefixLength)))
										prefixLength++;

									while ((suffixLength < suggestion.length() - prefixLength)
											&& (suffixLength < cleanedSearchWord.length() - prefixLength)
											&& (suggestion.charAt(suggestion.length() - suffixLength - 1) == cleanedSearchWord
													.charAt(cleanedSearchWord.length() - suffixLength - 1)))
										suffixLength++;

									if ((prefixLength > 0) || (suffixLength > 0)) {
										distance = cxpDamerauLevenshtein(
												cleanedSearchWord.substring(prefixLength, cleanedSearchWord.length() - suffixLength),
												suggestion.substring(prefixLength, suggestion.length() - suffixLength));
										
									} else {
										distance = cxpDamerauLevenshtein(cleanedSearchWord, suggestion);
									}
									distance = customizing.adjustDetailedDistance(cleanedSearchWord, suggestion, distance, prefixLength, suffixLength);
								}
							}

							// save some time.
							// remove all existing suggestions of higher
							// distance, if verbose<2
							if ((accuracyLevel.ordinal() < 2)
									&& (suggestions.size() > 0)
									&& (suggestions.get(0).distance > distance)) {
								suggestions.clear();
							}

							// do not process higher distances than those
							// already found, if recallLevel < 2
							if ((accuracyLevel.ordinal() < 2)
									&& (suggestions.size() > 0)
									&& (distance > suggestions.get(0).distance)) {
								continue;
							}

							distance = customizing.adjustDistance(cleanedSearchWord, candidate, distance);

							if (distance <= editDistanceMax) {
								Object suggestedItem = dictionary.get(suggestion);
								if (suggestedItem != null) {
									SuggestItem si = new SuggestItem();
									si.term = suggestion;
									si.count = ((DictionaryItem) suggestedItem).count;
									si.wordFrequency = ((double) si.count / dictionary.size());
									si.distance = distance;
									suggestions.add(si);
								}
							}
						}
					} // end for each
				} // end if: candidate exists in dictionary

				// add more edits to candidate list
				// derive edits (deletes) from current candidate and add them to
				// candidates list
				// this is a recursive process until the maximum edit distance
				// has been reached
				if (cleanedSearchWord.length() - candidate.length() < editDistanceMax) {
					// save some time: do not create edits with edit distance
					// smaller than suggestions already found
					if ((accuracyLevel.ordinal() < 2) && (suggestions.size() > 0) && (cleanedSearchWord.length() - candidate
							.length() >= suggestions.get(0).distance)) continue;

					for (int i = 0; i < candidate.length(); i++) {
						String delete = candidate.substring(0, i) + candidate.substring(i + 1);
						if (candidatesUniq.add(delete)) candidates.add(delete);
					}
				}
			} // end lable nosort
		} // end while

		return pickSuggestions(cleanedSearchWord, editDistanceMax, suggestions);
	}

	private List<SuggestItem> pickSuggestions(String searchWord, int editDistanceMax, List<SuggestItem> suggestions) {
		int k = suggestions.size();
		if ((accuracyLevel == AccuracyLevel.topHit) && (suggestions.size() > 1))
			k = 1;
		else if (suggestions.size() > topK) {
			k = topK;
		}

		List<SuggestItem> returnSuggestions;
		if (k >= suggestions.size()) {
			returnSuggestions = suggestions;
		} else {
			returnSuggestions = Ordering.from(distanceCountComparator).leastOf(suggestions, k);
		}
		
		return customizing.adjustFinalResult(searchWord, returnSuggestions);
	}

	/**
	 * Simple and fast calculation of edit distance, comparing only length of
	 * both strings and multiplying the delta with deletion / insertion weight.
	 * 
	 * This is exact if the one string is part of the other string. This method
	 * won't do this check however.
	 * 
	 * @param fromString
	 * @param toString
	 * @return
	 */
	private double getMaxDistance(String fromString, String toString) {
		boolean isDelete = fromString.length() > toString.length();
		return (isDelete ? deletionWeight : insertionWeight)
				* (isDelete ? fromString.length() - toString.length() : toString.length() - fromString.length());
	}

	private double cxpDamerauLevenshtein(String a, String b) {
		double[][] d = new double[b.length() + 1][a.length() + 1]; // 2d matrix

		// Step 1
		if (a.length() == 0) return b.length();
		if (b.length() == 0) return a.length();

		// Step 2
		for (int i = a.length(); i >= 0; i--)
			d[0][i] = i * deletionWeight;
		for (int j = b.length(); j >= 0; j--)
			d[j][0] = j;

		// Step 4
		for (int j = 1; j <= b.length(); j++) {
			char b_j = b.charAt(j - 1);

			// Step 3
			for (int i = 1; i <= a.length(); i++) {
				char a_i = a.charAt(i - 1);

				// CXP Damerau operations
				double min = min(
						d[j - 1][i - 1],
						d[j - 1][i],
						d[j][i - 1]);
				if (a_i == b_j) {
					d[j][i] = min;
				} else if (i == j) {
					d[j][i] = min + (replaceWeight * customizing.getReplacementDistance(b_j, a_i)); // replace

					if (i > 1 && a_i == b.charAt(j - 2) && a.charAt(i - 2) == b_j) {
						d[j][i] = Math.min(d[j][i], d[j - 2][i - 2] + transpositionWeight); // transpose
					}
				} else if (i > j) {
					d[j][i] = min + deletionWeight; // delete
				} else if (i < j) {
					d[j][i] = min + insertionWeight; // insert
				}
			}
		}
		// Step 5
		return d[b.length()][a.length()];
	}

	private double min(double a, double b, double c) {
		return Math.min(a, Math.min(b, c));
	}

	@Override
	public String toString() {
		return "PreDict "+customizing.toString();
	}
}
