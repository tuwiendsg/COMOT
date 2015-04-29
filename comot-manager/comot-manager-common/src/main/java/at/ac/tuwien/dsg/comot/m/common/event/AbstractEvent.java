package at.ac.tuwien.dsg.comot.m.common.event;

import java.io.Serializable;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({ LifeCycleEvent.class, CustomEvent.class, LifeCycleEventModifying.class })
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public abstract class AbstractEvent implements Serializable {

	private static final long serialVersionUID = -1441246623921549350L;

	@XmlAttribute
	protected String eventId;
	@XmlAttribute
	protected String correlationId;
	@XmlAttribute
	protected String serviceId;
	@XmlAttribute
	protected String groupId;
	@XmlAttribute
	protected String origin;
	@XmlAttribute
	protected Long time;

	public AbstractEvent() {
	}

	public AbstractEvent(String serviceId, String groupId, String origin, Long time) {
		super();
		this.eventId = UUID.randomUUID().toString();
		this.serviceId = serviceId;
		this.groupId = groupId;
		this.origin = origin;
		this.time = time;
	}

	// GENERATED

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

}
