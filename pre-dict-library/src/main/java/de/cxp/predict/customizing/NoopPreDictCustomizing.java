package de.cxp.predict.customizing;

import de.cxp.predict.api.PreDictSettings;
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
	public String toString() {
		return "SE";
	}
}
