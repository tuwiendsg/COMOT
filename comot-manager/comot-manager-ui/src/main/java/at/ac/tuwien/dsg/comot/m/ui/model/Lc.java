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
package at.ac.tuwien.dsg.comot.m.ui.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class Lc {

	@XmlElement(name = "nodes")
	List<LcState> states = new ArrayList<>();
	@XmlElement(name = "links")
	List<LcTransition> transitions = new ArrayList<>();

	public Lc() {

	}

	public void addTransition(State state, Action action, State nextState) {
		transitions.add(new LcTransition(action.toString(), stateNr(state), stateNr(nextState)));
	}

	protected Integer stateNr(State state) {
		for (int i = 0; i < states.size(); i++) {
			if (states.get(i).getName().equals(state)) {
				return i;
			}
		}
		return null;
	}

	public List<LcState> getStates() {
		return states;
	}

	public void setStates(List<LcState> states) {
		this.states = states;
	}

	public List<LcTransition> getTransitions() {
		return transitions;
	}

	public void setTransitions(List<LcTransition> transitions) {
		this.transitions = transitions;
	}

}
