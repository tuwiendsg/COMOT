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
package at.ac.tuwien.dsg.comot.m.common.exception;

import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;

public class ComotLifecycleException extends ComotException {

	private static final long serialVersionUID = 6270339245853337512L;

	protected LifeCycleEvent event;

	public ComotLifecycleException(String message, LifeCycleEvent event) {
		super(message);
		this.event = event;
	}

	public ComotLifecycleException(String message) {
		super(message);
	}

	public LifeCycleEvent getEvent() {
		return event;
	}

	public void setEvent(LifeCycleEvent event) {
		this.event = event;
	}

}
