package de.cxp.predict.api;

import de.cxp.predict.PreDict;
import de.cxp.predict.PreDict.AccuracyLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Data
@NoArgsConstructor
public class PreDictSettings {

	private int editDistanceMax = 2;

	private AccuracyLevel accuracyLevel = AccuracyLevel.maximum;

	private int topK = 6; // limits result to n entries

	// Damerau function variables
	private double deletionWeight = 0.8; // 1.20 1.40

	private double insertionWeight = 1.01; // 1.00

	private double replaceWeight = 1.0; // 1.20

	private double transpositionWeight = 1.05; // 1.05

	public PreDictSettings editDistanceMax(int editDistanceMax) {
		setEditDistanceMax(editDistanceMax);
		return this;
	}

	public PreDictSettings accuracyLevel(AccuracyLevel tophit) {
		setAccuracyLevel(tophit);
		return this;
	}
	
	public PreDictSettings topK(int topK) {
		setTopK(topK);
		return this;
	}

	public PreDictSettings deletionWeight(double weight) {
		setDeletionWeight(weight);
		return this;
	}

	public PreDictSettings insertionWeight(double weight) {
		setInsertionWeight(weight);
		return this;
	}

	public PreDictSettings replaceWeight(double weight) {
		setReplaceWeight(weight);
		return this;
	}

	public PreDictSettings transpositionWeight(double weight) {
		setTranspositionWeight(weight);
		return this;
	}
	
}
