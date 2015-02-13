package at.ac.tuwien.dsg.comot.m.common.exception;

/**
 * Thrown to propagate HTTP error from a core service.
 * 
 * @author Juraj
 *
 */
public class CoreServiceException extends Exception {

	private static final long serialVersionUID = 7350286958469243223L;

	protected int code;
	protected String msg;
	protected String componentName;
	protected boolean clientError;

	public CoreServiceException(int code, String msg, String componentName) {
		super("HTTP code=" + code + ", message='" + msg + "'");

		this.code = code;
		this.msg = msg;
		this.componentName = componentName;

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

	public String getComponentName() {
		return componentName;
	}

}
