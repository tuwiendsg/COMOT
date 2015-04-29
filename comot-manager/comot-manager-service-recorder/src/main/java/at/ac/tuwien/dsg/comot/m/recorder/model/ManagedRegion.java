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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ManagedRegion {

	public static final String LABEL_REGION = "_REGION";
	public static final String PROP_ID = "_id";
	public static final String PROP_TIMESTAMP = "_timestamp";

	protected Set<InternalNode> nodes;
	protected Set<InternalRel> relationships;

	protected Map<String, String> classes = new HashMap<>();
	protected InternalNode startNode;

	public ManagedRegion() {

	}

	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void addClass(String key, String value) {
		if (classes == null) {
			classes = new HashMap<>();
		}
		classes.put(key, value);
	}

	public void addNode(InternalNode node) {
		if (nodes == null) {
			nodes = new HashSet<>();
		}
		nodes.add(node);
	}

	public void addRelationship(InternalRel rel) {
		if (relationships == null) {
			relationships = new HashSet<>();
		}
		relationships.add(rel);
	}

	// GENERATED METHODS

	public Set<InternalNode> getNodes() {
		return nodes;
	}

	public void setNodes(Set<InternalNode> nodes) {
		this.nodes = nodes;
	}

	public Set<InternalRel> getRelationships() {
		return relationships;
	}

	public void setRelationships(Set<InternalRel> relationships) {
		this.relationships = relationships;
	}

	public Map<String, String> getClasses() {
		return classes;
	}

	public void setClasses(Map<String, String> classes) {
		this.classes = classes;
	}

	public InternalNode getStartNode() {
		return startNode;
	}

	public void setStartNode(InternalNode startNode) {
		this.startNode = startNode;
	}

}
