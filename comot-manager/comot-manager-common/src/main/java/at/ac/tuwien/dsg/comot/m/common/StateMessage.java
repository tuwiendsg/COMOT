package at.ac.tuwien.dsg.comot.m.common;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.data.neo4j.annotation.NodeEntity;

@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
@XmlRootElement
public class StateMessage {

	protected String targetedId;
	protected Action action;
	protected Map<String, Transition> transitions = new HashMap<>();

	public StateMessage() {

	}

	public StateMessage(String targetedId, Action action) {
		super();
		this.targetedId = targetedId;
		this.action = action;
	}

	public void addOne(String id, State oldState, State newState) {
		if (transitions == null) {
			transitions = new HashMap<>();
		}
		transitions.put(id, new Transition(oldState, newState));
	}

	public class Transition {
		State oldState;
		State newState;

		public Transition(State oldState, State newState) {
			super();
			this.oldState = oldState;
			this.newState = newState;
		}

		public State getOldState() {
			return oldState;
		}

		public void setOldState(State oldState) {
			this.oldState = oldState;
		}

		public State getNewState() {
			return newState;
		}

		public void setNewState(State newState) {
			this.newState = newState;
		}

	}

	public String getTargetedId() {
		return targetedId;
	}

	public Action getAction() {
		return action;
	}

	public Map<String, Transition> getTransitions() {
		return transitions;
	}

}
