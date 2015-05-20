/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
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

	private static final Logger LOG = LoggerFactory.getLogger(UtilsLc.class);

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

		LOG.trace("executeAction(parentState={}, currentState={}, action={} )", parentState, currentState, action);

		if (action == Action.ERROR || action == Action.TERMINATE) {
			return State.ERROR;
		}

		if (action == Action.START_TEST || action == Action.FINISH_TEST || action == Action.RECONFIGURE_ELASTICITY) {
			return currentState;
		}

		if (states.contains(currentState)) {
			for (LifeCycleTransition transition : transitions) {

				LOG.trace("{}", transition);
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

		LOG.debug("translateToAction(oldState={}, newState={}) : {}", oldState, newState, result);
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
