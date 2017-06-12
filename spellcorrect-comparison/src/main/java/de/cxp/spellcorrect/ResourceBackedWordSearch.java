package de.cxp.spellcorrect;

public interface ResourceBackedWordSearch extends WordSearch, AutoCloseable {

	void indexingDone();
	
}
