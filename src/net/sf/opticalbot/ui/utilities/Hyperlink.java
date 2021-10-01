package net.sf.opticalbot.ui.utilities;

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Utility class which provides static methods for opening hyperlinks in user's
 * default browser.
 */
public class Hyperlink {

	public static void open(URI uri) throws HyperlinkException {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop()
				: null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (IOException e1) {
				throw new HyperlinkException(e1);
			}
		} else
			throw new HyperlinkException(
					"Your java version does not support opening hyperlinks in browser.");
	}

	public static void open(URL url) throws HyperlinkException {
		try {
			open(url.toURI());
		} catch (URISyntaxException e) {
			throw new HyperlinkException(e);
		}
	}

	public static void open(String url) throws HyperlinkException {
		try {
			URL _url = new URL(url);
			open(_url);
		} catch (MalformedURLException e) {
			throw new HyperlinkException(e);
		}
	}

}
