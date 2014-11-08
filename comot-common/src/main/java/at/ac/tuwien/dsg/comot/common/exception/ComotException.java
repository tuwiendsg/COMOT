package at.ac.tuwien.dsg.comot.common.exception;

public class ComotException extends Exception {

	public ComotException(String message) {
		super(message);
	}

	public ComotException(String message, Exception cause) {
		super(message, cause);
	}

}
