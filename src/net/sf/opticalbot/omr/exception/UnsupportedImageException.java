package net.sf.opticalbot.omr.exception;

/**
 * This exception is thrown when the system tries to load an unsupported image
 * type.
 */
public class UnsupportedImageException extends Exception {

	private static final long serialVersionUID = 1L;

	public UnsupportedImageException() {
		super();
	}

	public UnsupportedImageException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UnsupportedImageException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedImageException(String message) {
		super(message);
	}

	public UnsupportedImageException(Throwable cause) {
		super(cause);
	}

}
