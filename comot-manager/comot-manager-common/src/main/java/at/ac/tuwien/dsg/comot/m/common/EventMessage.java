package at.ac.tuwien.dsg.comot.m.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class EventMessage {

	@XmlAttribute
	protected String serviceId;
	@XmlAttribute
	protected String csInstanceId;
	@XmlAttribute
	protected String groupId;
	@XmlAttribute
	protected String origin;
	protected String message;
	@XmlAttribute
	protected Action action; // not in custom
	@XmlAttribute
	protected String customEvent; // not in lifecycle
	protected CloudService service; // not in custom

	public EventMessage() {

	}

	public EventMessage(String serviceId, String csInstanceId, String groupId, String customEvent, String origin,
			String message) {
		super();
		this.serviceId = serviceId;
		this.csInstanceId = csInstanceId;
		this.groupId = groupId;
		this.customEvent = customEvent;
		this.message = message;
		this.origin = origin;
	}

	public EventMessage(String serviceId, String csInstanceId, String groupId, Action action, String origin,
			CloudService service,
			String message) {
		super();
		this.serviceId = serviceId;
		this.csInstanceId = csInstanceId;
		this.groupId = groupId;
		this.action = action;
		this.message = message;
		this.service = service;
		this.origin = origin;
	}

	public boolean isLifeCycleDefined() {
		return (action != null) ? true : false;
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

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public String getCustomEvent() {
		return customEvent;
	}

	public void setCustomEvent(String customEvent) {
		this.customEvent = customEvent;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "EventMessage [serviceId=" + serviceId + ", csInstanceId=" + csInstanceId + ", groupId=" + groupId
				+ (isLifeCycleDefined() ? (", action=" + action) : (", customEvent=" + customEvent)) +
				", origin=" + origin + ", message=" + message + "]";
	}

	public CloudService getService() {
		return service;
	}

	public void setService(CloudService service) {
		this.service = service;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

}
