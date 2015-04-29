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

import at.ac.tuwien.dsg.comot.m.common.enums.Action;
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
