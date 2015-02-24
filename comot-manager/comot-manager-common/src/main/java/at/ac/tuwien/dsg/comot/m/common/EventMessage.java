package at.ac.tuwien.dsg.comot.m.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.model.type.Action;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class EventMessage {

	protected String serviceId;
	protected String csInstanceId;
	protected String groupId;
	protected Action action;
	protected String customEvent;
	protected String message;

	public EventMessage() {

	}

	public EventMessage(String serviceId, String csInstanceId, String groupId, String customEvent, String message) {
		super();
		this.serviceId = serviceId;
		this.csInstanceId = csInstanceId;
		this.groupId = groupId;
		this.customEvent = customEvent;
		this.message = message;
	}

	public EventMessage(String serviceId, String csInstanceId, String groupId, Action action, String message) {
		super();
		this.serviceId = serviceId;
		this.csInstanceId = csInstanceId;
		this.groupId = groupId;
		this.action = action;
		this.message = message;
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
				+ ", action=" + action + ", customEvent=" + customEvent + ", message=" + message + "]";
	}

}
