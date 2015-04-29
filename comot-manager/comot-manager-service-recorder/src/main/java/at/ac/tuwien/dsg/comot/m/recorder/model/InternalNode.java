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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InternalNode {

	public static final String ID = "_id";
	public static final String LABEL_STATE_SUFFIX = "_State";

	private String label;
	private String businessId;

	private Map<String, Object> properties = new HashMap<>();
	private Set<InternalRel> relationships = new HashSet<>();

	public InternalNode() {

	}

	@Override
	public String toString() {
		return "(" + label + ": id=" + businessId + ", relC=" + relationships.size() + ", props=" + properties + ")";
	}

	public Collection<String> getLablesForIdentityNode() {
		List<String> list = new ArrayList<>();
		list.add(label);
		list.add(LabelTypes._IDENTITY.toString());
		return list;
	}

	public Collection<String> getLablesForStateNode() {
		List<String> list = new ArrayList<>();
		list.add(label + LABEL_STATE_SUFFIX);
		list.add(LabelTypes._STATE.toString());
		return list;
	}

	public Map<String, Object> getBusinessIdAsMap() {
		Map<String, Object> map = new HashMap<>();
		map.put(ID, businessId);
		return map;
	}

	public void addProperty(String key, Object value) {
		if (properties == null) {
			properties = new HashMap<>();
		}
		properties.put(key, value);
	}

	public void addRelationship(InternalRel rel) {
		if (relationships == null) {
			relationships = new HashSet<>();
		}
		relationships.add(rel);
	}

	// GENERATED METHODS

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getBusinessId() {
		return businessId;
	}

	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public Set<InternalRel> getRelationships() {
		return relationships;
	}

	public void setRelationships(Set<InternalRel> relationships) {
		this.relationships = relationships;
	}

}
