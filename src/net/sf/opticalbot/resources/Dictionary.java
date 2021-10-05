package net.sf.opticalbot.resources;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

// TODO USE JSON AND REMOVE COMMONS-IO DEPENDENCY
public class Dictionary extends Properties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// External directory where this class will scan for language files
	private static final String LANGUAGES_DIR = "src/resources/language/";

	private static Dictionary i18n = null;

	public Dictionary(String language) {
		super();
		try {
			String translationFile = LANGUAGES_DIR + language + ".lang";
			final FileInputStream translationInputStream = new FileInputStream(
					translationFile);

			InputStreamReader isr = new InputStreamReader(
					translationInputStream, "UTF-8");
			byte[] bs = new String(IOUtils.toByteArray(isr), "UTF-8")
					.getBytes("ISO-8859-1");
			isr.close();

			ByteArrayInputStream bais = new ByteArrayInputStream(bs);

			load(bais);
		} catch (IOException e) {
			// TODO throw exception and handle it properly!!!
			e.printStackTrace();
		}
	}

	public static void setDictionaryLanguage(String language) {
		i18n = new Dictionary(language);
	}

	public static String translate(String key) {
		return i18n.getProperty(key, key);
	}

}
