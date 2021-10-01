package net.sf.opticalbot.omr.exception;

/** This exception is thrown when there are problems when loading an OMRModel. */
public class OMRModelLoadException extends Exception {

	private static final long serialVersionUID = 1L;

	public OMRModelLoadException() {
		super();
	}

	public OMRModelLoadException(String message) {
		super(message);
	}

	public OMRModelLoadException(Throwable cause) {
		super(cause);
	}

	public OMRModelLoadException(String message, Throwable cause) {
		super(message, cause);
	}

	public OMRModelLoadException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
