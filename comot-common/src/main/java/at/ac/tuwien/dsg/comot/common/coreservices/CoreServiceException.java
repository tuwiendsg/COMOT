package at.ac.tuwien.dsg.comot.common.coreservices;

public class CoreServiceException extends Exception {

	protected int code;
	protected String msg;

	public CoreServiceException(int code, String msg) {
		super("HTTP code=" + code + ", message='" + msg + "'");
	}

	public CoreServiceException(String message) {
		super(message);
	}

	public CoreServiceException(String message, Exception cause) {
		super(message, cause);
	}

}
