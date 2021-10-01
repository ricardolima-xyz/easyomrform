package net.sf.opticalbot.ui.utilities;

/** This exception is thrown when there are problems when opening a Hyperlink. */
public class HyperlinkException extends Exception {

	private static final long serialVersionUID = 1L;

	public HyperlinkException() {
	}

	public HyperlinkException(String message) {
		super(message);
	}

	public HyperlinkException(Throwable cause) {
		super(cause);
	}

	public HyperlinkException(String message, Throwable cause) {
		super(message, cause);
	}

	public HyperlinkException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
