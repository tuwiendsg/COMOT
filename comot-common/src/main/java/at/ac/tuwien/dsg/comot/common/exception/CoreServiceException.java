package at.ac.tuwien.dsg.comot.common.exception;

public class CoreServiceException extends Exception {

	protected int code;
	protected String msg;
	protected boolean clientError;

	public CoreServiceException(int code, String msg) {
		super("HTTP code=" + code + ", message='" + msg + "'");

		if (code / 100 == 4) {
			clientError = true;
		}
	}

	public CoreServiceException(String message) {
		super(message);
	}

	public CoreServiceException(String message, Exception cause) {
		super(message, cause);
	}

	public int getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}

	public boolean isClientError() {
		return clientError;
	}

}
