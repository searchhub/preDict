package de.cxp.predict.customizing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import de.cxp.predict.api.CharDistance;
import de.cxp.predict.api.PreDictSettings;
import de.cxp.predict.api.SuggestItem;
import de.cxp.predict.common.Eudex;
import de.cxp.predict.common.FragmentProximity;
import de.cxp.predict.common.PrefixProximity;
import de.cxp.predict.common.QwertzKeyboardDistance;
import lombok.Getter;
import lombok.NonNull;

public class CommunityCustomization implements PreDictCustomizing {

	@NonNull
	@Getter
	private final PreDictSettings settings;
	
	// remove pattern for cleansing during search
	private static final Pattern removePattern = Pattern.compile("[^\\p{L}\\p{N}\\p{Z}]");

	private final CharDistance keyboardDistance = new QwertzKeyboardDistance();

	// Proximity function variables
	private final double editWeight = 1.2; // 0.9
	private final double phoneWeight = 1; // 0.2
	private final double prefixWeight = 1.4; // 0.8
	private final double fragmentWeight = 1.5; // 0.6
	private final double wordFrequencyWeight = 0; // 0.0
	private final double divisor = editWeight + phoneWeight + prefixWeight + fragmentWeight + wordFrequencyWeight;

	private final PrefixProximity prefixProximity = new PrefixProximity();
	
	private Comparator<SuggestItem> proximityComparator = new Comparator<SuggestItem>() {
		public int compare(SuggestItem x, SuggestItem y) {
			int compare = Double.compare(x.proximity, y.proximity);
			if (compare == 0) compare = Integer.compare(x.count, y.count);
			return -1 * compare;
		}
	};

	private double maxEditDistance;
	
	public CommunityCustomization(PreDictSettings settings) {
		this.settings = settings;
		maxEditDistance = settings.getEditDistanceMax();
	}

	@Override
	public String cleanIndexWord(String word) {
		// minimal normalization 
		return word.toLowerCase().replaceAll("\\s+", " ");
	}
	
	@Override
	public String cleanSearchWord(String searchWord) {
		return removePattern.matcher(searchWord).replaceAll("").toLowerCase();
	}
	
	@Override
	public double adjustDistance(String searchWord, String candidate, double distance) {
		return distance;
	}

	@Override
	public double adjustDetailedDistance(String searchWord, String suggestion, double distance, int prefixLength,
			int suffixLength) {
		return distance;
	}

	@Override
	public double getReplacementDistance(char a, char b) {
		return keyboardDistance.distance(a, b);
	}

	@Override
	public List<SuggestItem> adjustFinalResult(String searchWord, List<SuggestItem> result) {
		List<SuggestItem> sortedResult = new ArrayList<>();
		long searchWordEudex = Eudex.encode(searchWord);

		for (SuggestItem s : result) {
			s.editProximity = (maxEditDistance - s.distance) / maxEditDistance;

			s.phoneticProximity = Eudex.distance(searchWordEudex, Eudex.encode(s.term));
			// we are using EUDEX but have to normalize the distance by
			// 1-(LN(eudexDistance+1)/9)
			s.phoneticProximity = (1 - ((Math.log(s.phoneticProximity + 1) / Math.log(10) / 9)));

			s.fragmentProximity = FragmentProximity.distance(searchWord, s.term);
			s.prefixProximity = prefixProximity.distance(searchWord, s.term);

			s.proximity = getCombinedProximity(searchWord, s);

			sortedResult.add(s);
		}
		
		// sort by descending proximity, then by descending word
		// frequency/language probability
		Collections.sort(sortedResult, proximityComparator);
		return sortedResult;
	}

	protected double getCombinedProximity(String searchWord, SuggestItem s) {
		return (s.editProximity * editWeight
				+ s.phoneticProximity * phoneWeight
				+ s.fragmentProximity * fragmentWeight
				+ s.prefixProximity * prefixWeight
				+ s.wordFrequency * wordFrequencyWeight) / divisor;
	}

	@Override
	public String toString() {
		return "CE";
	}
}
