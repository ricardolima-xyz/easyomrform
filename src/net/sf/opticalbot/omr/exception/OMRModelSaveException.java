package net.sf.opticalbot.omr.exception;

/** This exception is thrown when there are problems when saving an OMRModel. */
public class OMRModelSaveException extends Exception {

	private static final long serialVersionUID = 1L;

	public OMRModelSaveException() {
		super();
	}

	public OMRModelSaveException(String message) {
		super(message);
	}

	public OMRModelSaveException(Throwable cause) {
		super(cause);
	}

	public OMRModelSaveException(String message, Throwable cause) {
		super(message, cause);
	}

	public OMRModelSaveException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
