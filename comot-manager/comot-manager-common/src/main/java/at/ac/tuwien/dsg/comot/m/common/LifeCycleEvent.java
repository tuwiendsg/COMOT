package at.ac.tuwien.dsg.comot.m.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class LifeCycleEvent extends AbstractEvent {

	private static final long serialVersionUID = -8400050596484796515L;

	@XmlAttribute
	protected Action action;
	protected CloudService service;

	public LifeCycleEvent() {

	}

	public LifeCycleEvent(String serviceId, String csInstanceId, String groupId, Action action, String origin,
			CloudService service) {
		super();
		this.serviceId = serviceId;
		this.csInstanceId = csInstanceId;
		this.groupId = groupId;
		this.action = action;
		this.service = service;
		this.origin = origin;
	}

	// GENERATED

	@Override
	public String toString() {
		return "LifeCycleEvent [action=" + action + ", serviceId=" + serviceId + ", csInstanceId=" + csInstanceId
				+ ", groupId=" + groupId + ", origin=" + origin + "]";
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public CloudService getService() {
		return service;
	}

	public void setService(CloudService service) {
		this.service = service;
	}

}
