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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.m.recorder.model.Change;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AnaliticsResult {

	protected UnitInstance instance;
	protected List<Change> events;

	public AnaliticsResult() {

	}

	public AnaliticsResult(UnitInstance instance, List<Change> events) {
		super();
		this.instance = instance;
		this.events = events;
	}

	public UnitInstance getInstance() {
		return instance;
	}

	public void setInstance(UnitInstance instance) {
		this.instance = instance;
	}

	public List<Change> getEvents() {
		return events;
	}

	public void setEvents(List<Change> events) {
		this.events = events;
	}

}
