package de.cxp.spellcorrect.predict;

import java.lang.reflect.Constructor;

import de.cxp.predict.PreDict;
import de.cxp.predict.PreDict.AccuracyLevel;
import de.cxp.predict.api.PreDictSettings;
import de.cxp.predict.customizing.CommunityCustomization;
import de.cxp.predict.customizing.PreDictCustomizing;
import de.cxp.spellcorrect.WordSearch;

public class PreDictFactory {
	
	public static WordSearch getCommunityEdition() {
		return getCommunityEdition(AccuracyLevel.topHit);
	}
	
	public static WordSearch getCommunityEdition(AccuracyLevel accuracyLevel) {
		return new PreDictWrapper(
				new PreDict(
						new CommunityCustomization(new PreDictSettings().accuracyLevel(accuracyLevel))));
	}
	
	public static WordSearch getEnterpriseEdition() {
		return getEnterpriseEdition(AccuracyLevel.maximum);
	}
	
	public static WordSearch getEnterpriseEdition(AccuracyLevel accuracyLevel) {
		try {
			// some manual object construction to avoid compile errors if that closed-source dependency is missing
			ClassLoader cl = PreDictFactory.class.getClassLoader();
			Class<?> cxpCustomizationClazz = null;
			while (cl != null && cxpCustomizationClazz == null) {
				try {
					cxpCustomizationClazz = cl.loadClass("de.cxp.predict.customizing.CxpCustomization");
				} catch (ClassNotFoundException e1) {
					cl = cl.getParent();
				}
			}
			
			if (cxpCustomizationClazz != null) {
				Constructor<?> constructor = cxpCustomizationClazz.getConstructor(PreDictSettings.class);
				PreDictSettings settings = new PreDictSettings().accuracyLevel(accuracyLevel).topK(4);
				PreDictCustomizing cxpCustomization = (PreDictCustomizing) constructor.newInstance(settings);
				return new PreDictWrapper(new PreDict(cxpCustomization));
			} else {
				throw new ClassNotFoundException("de.cxp.predict.customizing.CxpCustomization");
			}
		} catch (Exception e) {
			throw new RuntimeException("can't init CXP's PreDict Enterprise Edition", e);
		}
	}
	
}
