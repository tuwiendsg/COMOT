package at.ac.tuwien.dsg.comot.m.common.events;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.model.type.State;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Transition {

	@XmlAttribute
	protected String groupId;
	@XmlAttribute
	protected Type groupType;
	@XmlAttribute
	protected State currentState;
	@XmlAttribute
	protected State lastState;
	@XmlAttribute
	protected boolean fresh;

	public Transition() {

	}

	public Transition(String groupId, Type type, State lastState, State currentState, boolean fresh) {
		super();
		this.groupId = groupId;
		this.groupType = type;
		this.currentState = currentState;
		this.lastState = lastState;
		this.fresh = fresh;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public Type getGroupType() {
		return groupType;
	}

	public void setGroupType(Type type) {
		this.groupType = type;
	}

	public State getCurrentState() {
		return currentState;
	}

	public void setCurrentState(State currentState) {
		this.currentState = currentState;
	}

	public State getLastState() {
		return lastState;
	}

	public void setLastState(State lastState) {
		this.lastState = lastState;
	}

	public boolean isFresh() {
		return fresh;
	}

	public void setFresh(boolean fresh) {
		this.fresh = fresh;
	}

}
