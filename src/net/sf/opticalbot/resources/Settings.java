package net.sf.opticalbot.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.sf.opticalbot.omr.ShapeType;

public class Settings extends Properties {

	public static int MAX_THRESHOLD = 255;
	public static int MIN_THRESHOLD = 1;
	public static int MAX_DENSITY = 100;
	public static int MIN_DENSITY = 1;
	public static int MAX_SHAPESIZE = 200;
	public static int MIN_SHAPESIZE = 1;

	public enum Setting {
		Language("lang", "en"), Threshold("threshold", "127"), Density("density", "60"), ShapeSize("shape.size", "60"),
		Shape("shape.type", ShapeType.CIRCLE.name());

		private final String key;
		private final String defaultValue;

		private Setting(String key, String defaultValue) {
			this.key = key;
			this.defaultValue = defaultValue;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		public String getKey() {
			return key;
		}
	}

	private static final String SETTINGS_FILE = "resources/settings/settings.properties";
	private static final long serialVersionUID = 1L;

	public Settings() {
		super();
		File file = new File(SETTINGS_FILE);
		try {
			load(new FileInputStream(file));
		} catch (IOException e) {
			// TODO: Throw exception and handle it properly
			e.printStackTrace();
		}

	}

	public void store() {
		try {
			store(new FileOutputStream(SETTINGS_FILE), null);
		} catch (FileNotFoundException e) {
			// TODO: Throw exception and handle it properly
			e.printStackTrace();
		} catch (IOException e) {
			// TODO: Throw exception and handle it properly
			e.printStackTrace();
		}
	}

	public String get(Setting setting) {
		return super.getProperty(setting.getKey(), setting.getDefaultValue());
	}

	public void set(Setting setting, String value) {
		super.setProperty(setting.getKey(), value);
	}

}
