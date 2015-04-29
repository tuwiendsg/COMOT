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
package at.ac.tuwien.dsg.comot.m.common.event.state;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.m.common.enums.Type;
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

	@Override
	public String toString() {
		return "Transition [groupId=" + groupId + ", groupType=" + groupType + ", currentState=" + currentState
				+ ", lastState=" + lastState + ", fresh=" + fresh + "]";
	}

}
