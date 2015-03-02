package at.ac.tuwien.dsg.comot.m.common;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class StateMessage {

	protected EventMessage event;
	protected Map<String, Transition> transitions = new HashMap<>(); // not in custom

	public StateMessage() {

	}

	public StateMessage(EventMessage event) {
		super();
		this.event = event;
	}

	public StateMessage(EventMessage event, Map<String, Transition> transitions) {
		super();
		this.event = event;
		this.transitions = transitions;
	}

	public void addOne(String id, Type type, State oldState, State newState, boolean fresh) {
		if (transitions == null) {
			transitions = new HashMap<>();
		}
		transitions.put(id, new Transition(id, type, oldState, newState, fresh));
	}

	public EventMessage getEvent() {
		return event;
	}

	public void setEvent(EventMessage event) {
		this.event = event;
	}

	public String getServiceId() {
		return event.getServiceId();
	}

	public String getCsInstanceId() {
		return event.getCsInstanceId();
	}

	public String getGroupId() {
		return event.getGroupId();
	}

	public Action getAction() {
		return event.getAction();
	}

	public String getCustomEvent() {
		return event.getCustomEvent();
	}

	public String getMessage() {
		return event.getMessage();
	}

	public Map<String, Transition> getTransitions() {
		return transitions;
	}

	public void setTransitions(Map<String, Transition> transitions) {
		this.transitions = transitions;
	}

}
