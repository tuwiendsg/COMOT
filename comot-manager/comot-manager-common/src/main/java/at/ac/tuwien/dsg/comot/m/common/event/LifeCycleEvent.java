package at.ac.tuwien.dsg.comot.m.common.event;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.m.common.enums.Action;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class LifeCycleEvent extends AbstractEvent {

	private static final long serialVersionUID = -8400050596484796515L;

	@XmlAttribute
	protected Action action;

	public LifeCycleEvent() {

	}

	public LifeCycleEvent(String serviceId, String csInstanceId, String groupId, Action action) {
		super(serviceId, csInstanceId, groupId, null, null);
		this.action = action;
	}

	public LifeCycleEvent(String serviceId, String csInstanceId, String groupId, Action action, String origin, Long time) {
		super(serviceId, csInstanceId, groupId, origin, time);
		this.action = action;
	}

	// GENERATED

	@Override
	public String toString() {
		return "LifeCycleEvent [name=" + action + ", serviceId=" + serviceId + ", csInstanceId=" + csInstanceId
				+ ", groupId=" + groupId + ", origin=" + origin + "]";
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

}
