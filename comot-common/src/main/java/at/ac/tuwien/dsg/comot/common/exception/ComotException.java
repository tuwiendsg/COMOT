package at.ac.tuwien.dsg.comot.common.exception;

public class ComotException extends Exception {

	private static final long serialVersionUID = 1619965952788860347L;

	public ComotException(String message) {
		super(message);
	}

	public ComotException(String message, Exception cause) {
		super(message, cause);
	}

}
