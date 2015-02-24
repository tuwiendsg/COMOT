package at.ac.tuwien.dsg.comot.m.common;

import at.ac.tuwien.dsg.comot.model.type.State;

public class Transition {
	State oldState;
	State newState;

	public Transition() {

	}

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