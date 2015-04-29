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
package at.ac.tuwien.dsg.comot.m.core.analytics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.ActionEvent;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.CustomEvent;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.IEvent;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ActionReport {

	String targetId;
	String actionId;
	IEvent.Stage currentStage;
	Long timestamp;
	Set<String> effectedInstances;
	List<CustomEventReport> customEvents;

	public ActionReport() {

	}

	public ActionReport(ActionEvent event, Long timestamp) {
		super();
		this.targetId = event.getTargetId();
		this.actionId = event.getActionId();
		this.timestamp = timestamp;
		currentStage = IEvent.Stage.START;
	}

	public void addEffectedInstances(String id) {

		if (effectedInstances == null) {
			effectedInstances = new HashSet<>();
		}
		effectedInstances.add(id);
	}

	public void addCustomEvent(CustomEvent event, Long timestamp) {

		if (customEvents == null) {
			customEvents = new ArrayList<>();
		}
		customEvents.add(new CustomEventReport(event, timestamp));
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public IEvent.Stage getCurrentStage() {
		return currentStage;
	}

	public void setCurrentStage(IEvent.Stage currentStage) {
		this.currentStage = currentStage;
	}

	public List<CustomEventReport> getCustomEvents() {
		return customEvents;
	}

	public void setCustomEvents(List<CustomEventReport> customEvents) {
		this.customEvents = customEvents;
	}

	public Set<String> getEffectedInstances() {
		return effectedInstances;
	}

	public void setEffectedInstances(Set<String> effectedInstances) {
		this.effectedInstances = effectedInstances;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

}
