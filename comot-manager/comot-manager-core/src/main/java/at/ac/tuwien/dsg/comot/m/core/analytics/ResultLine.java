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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ResultLine {

	@XmlAttribute(name = "ServiceID")
	String serviceId;
	@XmlAttribute(name = "InstanceID")
	String instanceId;
	@XmlAttribute(name = "UnitID")
	String unitId;
	@XmlAttribute(name = "Type")
	String type;
	@XmlAttribute(name = "Timestamp")
	Long timestamp;
	@XmlAttribute(name = "Length")
	Double length; // seconds
	@XmlAttribute(name = "Stage")
	String stage;

	public ResultLine() {

	}

	public ResultLine(String serviceId, String instanceId, String unitId, String type, Long timestamp, Double length,
			String stage) {
		super();
		this.serviceId = serviceId;
		this.instanceId = instanceId;
		this.unitId = unitId;
		this.type = type;
		this.timestamp = timestamp;
		this.length = length;
		this.stage = stage;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Double getLength() {
		return length;
	}

	public void setLength(Double length) {
		this.length = length;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	@Override
	public String toString() {
		return "ResultLine [instanceId=" + instanceId + ", unitId=" + unitId + ", type=" + type + ", timestamp="
				+ timestamp + ", length=" + length + ", stage=" + stage + "]";
	}

}
