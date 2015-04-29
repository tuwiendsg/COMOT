package at.ac.tuwien.dsg.comot.m.common.event;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class CustomEvent extends AbstractEvent {

	private static final long serialVersionUID = -7049148070927588180L;

	@XmlAttribute
	protected String customEvent;
	@XmlAttribute
	protected String epsId;
	protected String message;

	public CustomEvent() {

	}

	public CustomEvent(String serviceId, String groupId, String customEvent, String epsId,
			String message) {
		super(serviceId, groupId, null, null);
		this.customEvent = customEvent;
		this.epsId = epsId;
		this.message = message;
	}

	public CustomEvent(String serviceId, String groupId, String customEvent, String origin,
			Long time, String epsId, String message) {
		super(serviceId, groupId, origin, time);
		this.customEvent = customEvent;
		this.epsId = epsId;
		this.message = message;
	}

	// GENERATED

	@Override
	public String toString() {
		return "CustomEvent [name=" + customEvent + ", epsId=" + epsId + ", serviceId=" + serviceId
				+ ", groupId=" + groupId + ", origin=" + origin + "]";
	}

	public String getCustomEvent() {
		return customEvent;
	}

	public void setCustomEvent(String customEvent) {
		this.customEvent = customEvent;
	}

	public String getEpsId() {
		return epsId;
	}

	public void setEpsId(String epsId) {
		this.epsId = epsId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
