package de.cxp.predict.common;

import java.util.HashMap;

import de.cxp.predict.api.CharDistance;

public class QwertzKeyboardDistance implements CharDistance {

	@Override
	public double distance(char a, char b) {
		return qwertzKeyboardDistance(a, b);
	}
	
	private static double qwertzKeyboardDistance(char a, char b) {
		String key1 = String.valueOf(a) + String.valueOf(b);
		String key2 = String.valueOf(b) + String.valueOf(a);
		if (qwertzCharMap.containsKey(key1)) return (1 - qwertzCharMap.get(key1));
		if (qwertzCharMap.containsKey(key2)) return (1 - qwertzCharMap.get(key2));
		else return 1;
	}
	
	// CXP QWERTY Keyboard Proximity CharMap - dependent on keyboard type
	static HashMap<String, Double> qwertzCharMap = new HashMap<String, Double>();
	static {
		double d1 = 0.4; // weight for keyboard proximity = 1
		double d2 = 0.1; // weight for keyboard proximity = 2
		qwertzCharMap.put("aq", d1);
		qwertzCharMap.put("as", d1);
		qwertzCharMap.put("aw", d1);
		qwertzCharMap.put("ay", d1);
		qwertzCharMap.put("ac", d2);
		qwertzCharMap.put("ad", d2);
		qwertzCharMap.put("ae", d2);
		qwertzCharMap.put("ar", d2);
		qwertzCharMap.put("ax", d2);

		qwertzCharMap.put("bg", d1);
		qwertzCharMap.put("bh", d1);
		qwertzCharMap.put("bn", d1);
		qwertzCharMap.put("bv", d1);
		qwertzCharMap.put("bc", d2);
		qwertzCharMap.put("bf", d2);
		qwertzCharMap.put("bt", d2);
		qwertzCharMap.put("bz", d2);
		qwertzCharMap.put("bj", d2);
		qwertzCharMap.put("bm", d2);

		qwertzCharMap.put("cd", d1);
		qwertzCharMap.put("cf", d1);
		qwertzCharMap.put("cv", d1);
		qwertzCharMap.put("cx", d1);
		qwertzCharMap.put("cy", d2);
		qwertzCharMap.put("cs", d2);
		qwertzCharMap.put("ce", d2);
		qwertzCharMap.put("cr", d2);
		qwertzCharMap.put("cg", d2);
		qwertzCharMap.put("cb", d2);

		qwertzCharMap.put("de", d1);
		qwertzCharMap.put("df", d1);
		qwertzCharMap.put("dr", d1);
		qwertzCharMap.put("ds", d1);
		qwertzCharMap.put("dx", d1);
		qwertzCharMap.put("dy", d2);
		qwertzCharMap.put("da", d2);
		qwertzCharMap.put("dt", d2);
		qwertzCharMap.put("dg", d2);
		qwertzCharMap.put("dv", d2);

		qwertzCharMap.put("er", d1);
		qwertzCharMap.put("es", d1);
		qwertzCharMap.put("ew", d1);
		qwertzCharMap.put("eq", d2);
		qwertzCharMap.put("ex", d2);
		qwertzCharMap.put("ef", d2);
		qwertzCharMap.put("et", d2);
		qwertzCharMap.put("e3", d2);
		qwertzCharMap.put("e4", d2);

		qwertzCharMap.put("fc", d1);
		qwertzCharMap.put("fd", d1);
		qwertzCharMap.put("fg", d1);
		qwertzCharMap.put("fr", d1);
		qwertzCharMap.put("ft", d1);
		qwertzCharMap.put("fv", d1);
		qwertzCharMap.put("fh", d2);
		qwertzCharMap.put("fs", d2);
		qwertzCharMap.put("fx", d2);
		qwertzCharMap.put("fz", d2);

		qwertzCharMap.put("gh", d1);
		qwertzCharMap.put("gt", d1);
		qwertzCharMap.put("gv", d1);
		qwertzCharMap.put("gz", d1);
		qwertzCharMap.put("gu", d2);
		qwertzCharMap.put("gj", d2);
		qwertzCharMap.put("gn", d2);
		qwertzCharMap.put("gr", d2);

		qwertzCharMap.put("hb", d1);
		qwertzCharMap.put("hg", d1);
		qwertzCharMap.put("hj", d1);
		qwertzCharMap.put("hn", d1);
		qwertzCharMap.put("hu", d1);
		qwertzCharMap.put("hz", d1);
		qwertzCharMap.put("hi", d2);
		qwertzCharMap.put("hk", d2);
		qwertzCharMap.put("hm", d2);
		qwertzCharMap.put("hv", d2);
		qwertzCharMap.put("hr", d2);

		qwertzCharMap.put("ij", d1);
		qwertzCharMap.put("ik", d1);
		qwertzCharMap.put("io", d1);
		qwertzCharMap.put("iu", d1);
		qwertzCharMap.put("ip", d2);
		qwertzCharMap.put("im", d2);
		qwertzCharMap.put("in", d2);
		qwertzCharMap.put("iz", d2);
		qwertzCharMap.put("i8", d2);
		qwertzCharMap.put("i9", d2);

		qwertzCharMap.put("jh", d1);
		qwertzCharMap.put("ji", d1);
		qwertzCharMap.put("jk", d1);
		qwertzCharMap.put("jm", d1);
		qwertzCharMap.put("jn", d1);
		qwertzCharMap.put("ju", d1);
		qwertzCharMap.put("jo", d2);
		qwertzCharMap.put("jl", d2);
		qwertzCharMap.put("jt", d2);

		qwertzCharMap.put("ki", d1);
		qwertzCharMap.put("kj", d1);
		qwertzCharMap.put("kl", d1);
		qwertzCharMap.put("km", d1);
		qwertzCharMap.put("ko", d1);
		qwertzCharMap.put("kp", d2);
		qwertzCharMap.put("kn", d2);
		qwertzCharMap.put("ku", d2);

		qwertzCharMap.put("lk", d1);
		qwertzCharMap.put("lo", d1);
		qwertzCharMap.put("lp", d1);
		qwertzCharMap.put("lm", d2);
		qwertzCharMap.put("lu", d2);

		qwertzCharMap.put("mj", d1);
		qwertzCharMap.put("mk", d1);
		qwertzCharMap.put("mn", d1);
		qwertzCharMap.put("mu", d2);

		qwertzCharMap.put("nu", d2);
		qwertzCharMap.put("nz", d2);
		qwertzCharMap.put("nv", d2);

		qwertzCharMap.put("op", d1);
		qwertzCharMap.put("ou", d2);
		qwertzCharMap.put("o9", d2);
		qwertzCharMap.put("o0", d2);

		qwertzCharMap.put("qw", d1);
		qwertzCharMap.put("qy", d2);
		qwertzCharMap.put("qx", d2);
		qwertzCharMap.put("q1", d2);
		qwertzCharMap.put("q2", d2);

		qwertzCharMap.put("rt", d1);
		qwertzCharMap.put("rw", d2);
		qwertzCharMap.put("rs", d2);
		qwertzCharMap.put("rx", d2);
		qwertzCharMap.put("rv", d2);
		qwertzCharMap.put("rz", d2);
		qwertzCharMap.put("r4", d2);
		qwertzCharMap.put("r5", d2);

		qwertzCharMap.put("sw", d1);
		qwertzCharMap.put("sx", d1);
		qwertzCharMap.put("sy", d1);

		qwertzCharMap.put("tz", d1);
		qwertzCharMap.put("tv", d2);
		qwertzCharMap.put("tu", d2);
		qwertzCharMap.put("t5", d2);
		qwertzCharMap.put("t6", d2);

		qwertzCharMap.put("uz", d1);
		qwertzCharMap.put("u7", d2);
		qwertzCharMap.put("u8", d2);

		qwertzCharMap.put("vx", d2);

		qwertzCharMap.put("wy", d2);
		qwertzCharMap.put("wx", d2);
		qwertzCharMap.put("w2", d2);
		qwertzCharMap.put("w3", d2);

		qwertzCharMap.put("xy", d1);
	}
	
}
