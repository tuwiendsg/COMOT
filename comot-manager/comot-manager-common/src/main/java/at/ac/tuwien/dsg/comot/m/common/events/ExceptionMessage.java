package at.ac.tuwien.dsg.comot.m.common.events;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

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
	// TODO not being serialized
	protected Exception exception;

	public ExceptionMessage() {

	}

	public ExceptionMessage(String serviceId, String csInstanceId, String origin, Long time, Exception exception) {
		super();
		this.csInstanceId = csInstanceId;
		this.origin = origin;
		this.exception = exception;
		this.serviceId = serviceId;
		this.time = time;
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

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
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

	@Override
	public String toString() {
		return "ExceptionMessage [serviceId=" + serviceId + ", csInstanceId=" + csInstanceId + ", origin=" + origin
				+ ", exception=" + exception + "]";
	}

}
