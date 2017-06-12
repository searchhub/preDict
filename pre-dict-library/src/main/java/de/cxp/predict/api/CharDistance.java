package de.cxp.predict.api;

public interface CharDistance {

	/**
	 * returns a value between 0 and 1 (both inclusive).
	 * 0 for no distance and 1 for maximum distance.
	 * 
	 * @param a
	 * @param b
	 * @return distance value
	 */
	double distance(char a, char b);

}
