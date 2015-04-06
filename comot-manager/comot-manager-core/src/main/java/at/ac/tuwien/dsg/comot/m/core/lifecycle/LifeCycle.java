package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.adapter.UtilsLc;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

public class LifeCycle {

	private static final Logger log = LoggerFactory.getLogger(UtilsLc.class);

	protected Set<State> states = new HashSet<>();
	protected List<LifeCycleTransition> transitions = new ArrayList<>();

	public LifeCycle() {
		states.add(State.ERROR);
	}

	public boolean hasState(State state) {
		if (states.contains(state)) {
			return true;
		}
		return false;
	}

	public State executeAction(State parentState, State currentState, Action action) {

		log.trace("executeAction(parentState={}, currentState={}, action={} )", parentState, currentState, action);

		if (action == Action.ERROR) {
			return State.ERROR;
		}

		if (states.contains(currentState)) {
			for (LifeCycleTransition transition : transitions) {

				log.trace("{}", transition);
				if (transition.getState() == currentState && transition.getAction() == action) {
					if (transition.getParentState() == null) {
						return transition.nextState;
					} else {
						if (transition.getParentState() == parentState) {
							return transition.nextState;
						}
					}
				}
			}
		}
		return null;
	}

	public Action translateToAction(State oldState, State newState) {

		Action result = null;

		for (LifeCycleTransition tr : transitions) {
			if (tr.getState() == oldState && tr.getNextState() == newState) {
				result = tr.getAction();
				break;
			}
		}

		log.debug("translateToAction(oldState={}, newState={}) : {}", oldState, newState, result);
		return result;
	}

	public void addTransition(State state, Action action, State nextState) {
		states.add(state);
		states.add(nextState);
		transitions.add(new LifeCycleTransition(state, action, nextState));
	}

	public void addTransition(State state, Action action, State nextState, State parentState) {
		states.add(state);
		states.add(nextState);
		transitions.add(new LifeCycleTransition(state, action, nextState, parentState));
	}

	public Set<State> getStates() {
		return states;
	}

	public List<LifeCycleTransition> getTransitions() {
		return transitions;
	}

}
