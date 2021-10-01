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

	// TODO Use this enum instead of the constants.
	public enum Setting {
		Language("lang", "en"),
		Threshold("threshold", "127"),
		Density("density", "60"),
		ShapeSize("shape.size", "60"),
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

	public static final String LANG = "lang";
	public static final String DEFAULT_LANG = "en";

	// TODO To be deprecated - these settings will go on template file
	public static final String THRESHOLD = "threshold";
	private static final String DEFAULT_THRESHOLD = "127";

	// TODO To be deprecated - these settings will go on template file
	public static final String DENSITY = "density";
	private static final String DEFAULT_DENSITY = "60";

	// TODO To be deprecated - these settings will go on template file
	public static final String SHAPE_SIZE = "shape.size";
	private static final String DEFAULT_SHAPE_SIZE = "10";

	// TODO To be deprecated - these settings will go on template file
	public static final String SHAPE_TYPE = "shape.type";
	private static final String DEFAULT_SHAPE_TYPE = "CIRCLE";

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

	// TODO After removing all deprecated usages from code, remove these
	// Overridden methods, because our methods get and set use them!

	@Override
	@Deprecated
	/** Discouraged usage. Use get instead. */
	public String getProperty(String key) {
		return super.getProperty(key);
	}

	@Override
	@Deprecated
	/** Discouraged usage. Use get instead. */
	public String getProperty(String key, String defaultValue) {
		return super.getProperty(key, defaultValue);
	}

	@Override
	@Deprecated
	/** Discouraged usage. Use set instead. */
	public synchronized Object setProperty(String key, String value) {
		return super.setProperty(key, value);
	}

	public int getDensity() {
		return Integer.valueOf(getProperty(DENSITY, DEFAULT_DENSITY));
	}

	public int getThreshold() {
		return Integer.valueOf(getProperty(THRESHOLD, DEFAULT_THRESHOLD));
	}

	public int getShapeSize() {
		return Integer.valueOf(getProperty(SHAPE_SIZE, DEFAULT_SHAPE_SIZE));
	}

	public ShapeType getShapeType() {
		return ShapeType.valueOf(getProperty(SHAPE_TYPE, DEFAULT_SHAPE_TYPE));
	}
}
