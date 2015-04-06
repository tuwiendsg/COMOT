package at.ac.tuwien.dsg.comot.m.common.event.state;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.exception.ExceptionUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class ExceptionMessage extends ComotMessage {

	private static final long serialVersionUID = -3675993125733600072L;

	@XmlAttribute
	protected String serviceId;
	@XmlAttribute
	protected String csInstanceId;
	@XmlAttribute
	protected String origin;
	@XmlAttribute
	protected Long time;

	protected String type;
	protected String message;
	protected String details;

	public ExceptionMessage() {

	}

	public ExceptionMessage(String serviceId, String csInstanceId, String origin, Long time, Exception exception) {
		super();

		Throwable root = ExceptionUtils.getRootCause(exception);
		String type;
		String message;
		String details;

		if (root == null) {
			type = exception.getClass().getName();
			message = exception.getMessage();
			details = ExceptionUtils.getStackTrace(exception);
		} else {
			type = root.getClass().getName();
			message = root.getMessage();
			details = ExceptionUtils.getStackTrace(root);
		}

		this.csInstanceId = csInstanceId;
		this.origin = origin;
		this.type = type;
		this.message = message;
		this.details = details;
		this.serviceId = serviceId;
		this.time = time;
	}

	public ExceptionMessage(String serviceId, String csInstanceId, String origin, Long time, String type,
			String message,
			String details) {
		super();
		this.serviceId = serviceId;
		this.csInstanceId = csInstanceId;
		this.origin = origin;
		this.time = time;
		this.type = type;
		this.message = message;
		this.details = details;
	}

	public ExceptionMessage(String serviceId, String csInstanceId, String origin) {
		super();
		this.serviceId = serviceId;
		this.csInstanceId = csInstanceId;
		this.origin = origin;
		this.time = System.currentTimeMillis();
	}

	public String getCsInstanceId() {
		return csInstanceId;
	}

	public void setCsInstanceId(String csInstanceId) {
		this.csInstanceId = csInstanceId;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	@Override
	public String toString() {
		return "ExceptionMessage [serviceId=" + serviceId + ", csInstanceId=" + csInstanceId + ", origin=" + origin
				+ ", time=" + time + ", message=" + message + ", details=" + details + "]";
	}

}
