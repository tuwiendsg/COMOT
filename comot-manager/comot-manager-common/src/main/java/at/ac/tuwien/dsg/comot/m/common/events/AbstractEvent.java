package at.ac.tuwien.dsg.comot.m.common.events;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({ LifeCycleEvent.class, CustomEvent.class, ModifyingLifeCycleEvent.class })
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public abstract class AbstractEvent implements Serializable {

	private static final long serialVersionUID = -1441246623921549350L;

	@XmlAttribute
	protected String serviceId;
	@XmlAttribute
	protected String csInstanceId;
	@XmlAttribute
	protected String groupId;
	@XmlAttribute
	protected String origin;
	@XmlAttribute
	protected Long time;

	public AbstractEvent() {
	}

	public AbstractEvent(String serviceId, String csInstanceId, String groupId, String origin, Long time) {
		super();
		this.serviceId = serviceId;
		this.csInstanceId = csInstanceId;
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

	public String getCsInstanceId() {
		return csInstanceId;
	}

	public void setCsInstanceId(String csInstanceId) {
		this.csInstanceId = csInstanceId;
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

}
