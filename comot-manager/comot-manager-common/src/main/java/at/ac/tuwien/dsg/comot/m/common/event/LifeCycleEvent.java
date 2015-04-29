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
package at.ac.tuwien.dsg.comot.m.common.event;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.m.common.enums.Action;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class LifeCycleEvent extends AbstractEvent {

	private static final long serialVersionUID = -8400050596484796515L;

	@XmlAttribute
	protected Action action;

	public LifeCycleEvent() {

	}

	public LifeCycleEvent(String serviceId, String groupId, Action action) {
		super(serviceId, groupId, null, null);
		this.action = action;
	}

	public LifeCycleEvent(String serviceId, String groupId, Action action, String origin, Long time) {
		super(serviceId, groupId, origin, time);
		this.action = action;
	}

	// GENERATED

	@Override
	public String toString() {
		return "LifeCycleEvent [name=" + action + ", serviceId=" + serviceId + ", groupId=" + groupId + ", origin="
				+ origin + "]";
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

}
