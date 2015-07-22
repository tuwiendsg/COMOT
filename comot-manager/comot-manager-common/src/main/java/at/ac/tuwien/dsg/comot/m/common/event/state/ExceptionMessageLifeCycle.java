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
import javax.xml.bind.annotation.XmlRootElement;

import at.ac.tuwien.dsg.comot.m.common.event.AbstractEvent;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class ExceptionMessageLifeCycle extends ExceptionMessage {

	private static final long serialVersionUID = -601877313539183398L;

	protected AbstractEvent event;

	public ExceptionMessageLifeCycle() {

	}

	public ExceptionMessageLifeCycle(String serviceId, String origin, Long time, String type,
			String message, String details, AbstractEvent event) {
		super(serviceId, origin, time, type, message, details, event.getEventId());
		this.event = event;
	}

	public AbstractEvent getEvent() {
		return event;
	}

	public void setEvent(AbstractEvent event) {
		this.event = event;
	}

}
