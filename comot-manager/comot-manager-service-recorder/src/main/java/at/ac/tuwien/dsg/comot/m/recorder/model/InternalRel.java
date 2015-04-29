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
package at.ac.tuwien.dsg.comot.m.recorder.model;

import java.util.HashMap;
import java.util.Map;

public class InternalRel {

	public static final String PROPERTY_FROM = "from";
	public static final String PROPERTY_TO = "to";

	protected String type;
	protected InternalNode startNode;
	protected InternalNode endNode;

	private Map<String, Object> properties = new HashMap<>();

	public InternalRel() {

	}

	public InternalRel(String type, InternalNode startNode, InternalNode endNode, Map<String, Object> properties) {
		super();
		this.type = type;
		this.startNode = startNode;
		this.endNode = endNode;
		this.properties = properties;
	}

	// GENERATED METHODS

	@Override
	public String toString() {
		return " (" + startNode.getBusinessId() + ") - [" + type + "] -> (" + endNode.getBusinessId() + ") props="
				+ properties;
	}

	public InternalNode getStartNode() {
		return startNode;
	}

	public void setStartNode(InternalNode startNode) {
		this.startNode = startNode;
	}

	public InternalNode getEndNode() {
		return endNode;
	}

	public void setEndNode(InternalNode endNode) {
		this.endNode = endNode;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
