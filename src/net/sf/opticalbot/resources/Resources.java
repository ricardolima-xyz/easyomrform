package net.sf.opticalbot.resources;

import java.io.File;

import javax.swing.ImageIcon;

public class Resources {

	private static String iconsPath = "src/resources/icons/";
	private static String licenseFile = "src/resources/license/license.txt";

	public static ImageIcon getIcon(String key) {
		ImageIcon icon = new ImageIcon(iconsPath + key);
		return icon;
	}

	public static File getLicense() {
		return new File(licenseFile);
	}
}
