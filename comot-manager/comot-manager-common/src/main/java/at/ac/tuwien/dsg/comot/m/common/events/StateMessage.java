package at.ac.tuwien.dsg.comot.m.common.events;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.model.type.State;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class StateMessage extends ComotMessage {

	private static final long serialVersionUID = -8003674639140083439L;

	protected AbstractEvent event;
	protected Map<String, Transition> transitions = new HashMap<>();

	public StateMessage() {

	}

	public StateMessage(AbstractEvent event, Map<String, Transition> transitions) {
		super();
		this.event = event;
		this.transitions = transitions;
	}

	public boolean isLifeCycleDefined() {
		if (event.getClass().equals(LifeCycleEvent.class)) {
			return true;
		} else {
			return false;
		}
	}

	public void addOne(String id, Type type, State oldState, State newState, boolean fresh) {
		if (transitions == null) {
			transitions = new HashMap<>();
		}
		transitions.put(id, new Transition(id, type, oldState, newState, fresh));
	}

	public AbstractEvent getEvent() {
		return event;
	}

	public void setEvent(AbstractEvent event) {
		this.event = event;
	}

	public Map<String, Transition> getTransitions() {
		return transitions;
	}

	public void setTransitions(Map<String, Transition> transitions) {
		this.transitions = transitions;
	}

}
