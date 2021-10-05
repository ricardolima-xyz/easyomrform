package net.sf.opticalbot.resources;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class Languages {

	private static final String LANGUAGES_DIR = "src/resources/language/";

	public static Set<String> getAvailableLanguages() {
		Set<String> result = new HashSet<String>();

		File languagesDir = new File(LANGUAGES_DIR);
		if (languagesDir.exists()) {
			for (File f : languagesDir.listFiles()) {
				result.add(f.getName().replace(".lang", ""));
			}
		}
		return result;
	}
}
