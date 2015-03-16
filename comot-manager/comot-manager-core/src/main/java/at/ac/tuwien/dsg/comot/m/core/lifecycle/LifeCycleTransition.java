package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

public class LifeCycleTransition {

	protected State state;
	protected Action action;
	protected State nextState;
	protected State parentState;

	public LifeCycleTransition() {

	}

	public LifeCycleTransition(State state, Action action, State nextState) {
		super();
		this.state = state;
		this.action = action;
		this.nextState = nextState;
	}

	public LifeCycleTransition(State state, Action action, State nextState, State parentState) {
		super();
		this.state = state;
		this.action = action;
		this.nextState = nextState;
		this.parentState = parentState;
	}

	public State getState() {
		return state;
	}

	public Action getAction() {
		return action;
	}

	public State getNextState() {
		return nextState;
	}

	public State getParentState() {
		return parentState;
	}

	@Override
	public String toString() {
		return "Transition [state=" + state + ", action=" + action + ", nextState=" + nextState + ", parentState="
				+ parentState + "]";
	}

}
