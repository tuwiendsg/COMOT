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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class CustomEvent extends AbstractEvent {

	private static final long serialVersionUID = -7049148070927588180L;

	@XmlAttribute
	protected String customEvent;
	@XmlAttribute
	protected String epsId;
	protected String message;

	public CustomEvent() {

	}

	public CustomEvent(String serviceId, String groupId, String customEvent, String epsId,
			String message) {
		super(serviceId, groupId, null, null);
		this.customEvent = customEvent;
		this.epsId = epsId;
		this.message = message;
	}

	public CustomEvent(String serviceId, String groupId, String customEvent, String origin,
			Long time, String epsId, String message) {
		super(serviceId, groupId, origin, time);
		this.customEvent = customEvent;
		this.epsId = epsId;
		this.message = message;
	}

	// GENERATED

	@Override
	public String toString() {
		return "CustomEvent [name=" + customEvent + ", epsId=" + epsId + ", serviceId=" + serviceId
				+ ", groupId=" + groupId + ", origin=" + origin + "]";
	}

	public String getCustomEvent() {
		return customEvent;
	}

	public void setCustomEvent(String customEvent) {
		this.customEvent = customEvent;
	}

	public String getEpsId() {
		return epsId;
	}

	public void setEpsId(String epsId) {
		this.epsId = epsId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
