package at.ac.tuwien.dsg.comot.recorder;

public class RecorderException extends Exception {

	private static final long serialVersionUID = -6729789370162354975L;

	public RecorderException(String message) {
		super(message);
	}

	public RecorderException(String message, Exception cause) {
		super(message, cause);
	}

}
