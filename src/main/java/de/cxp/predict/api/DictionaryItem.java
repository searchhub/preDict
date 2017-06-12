package de.cxp.predict.api;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class DictionaryItem {
	
	public DictionaryItem() {
	}
	
	public DictionaryItem(int suggestion) {
		suggestions.add(suggestion);
	}
	
	public TIntList suggestions = new TIntArrayList();
	public int count = 0;
}