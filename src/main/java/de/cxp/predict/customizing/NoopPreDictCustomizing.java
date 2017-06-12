package de.cxp.predict.customizing;

import java.util.List;

import de.cxp.predict.api.PreDictSettings;
import de.cxp.predict.api.SuggestItem;
import lombok.Getter;

public class NoopPreDictCustomizing implements PreDictCustomizing {

	@Getter
	private final PreDictSettings settings;

	public NoopPreDictCustomizing() {
		this(new PreDictSettings());
	}
	
	public NoopPreDictCustomizing(PreDictSettings settings) {
		this.settings = settings;
	}

	@Override
	public String cleanIndexWord(String word) {
		return word;
	}

	@Override
	public String cleanSearchWord(String searchWord) {
		return searchWord;
	}

	@Override
	public double getReplacementDistance(char a, char b) {
		return 1;
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
	public List<SuggestItem> adjustFinalResult(String searchWord, List<SuggestItem> result) {
		return result;
	}

	@Override
	public String toString() {
		return "SE";
	}
}
