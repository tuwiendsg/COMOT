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

import java.io.Serializable;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;
import org.springframework.data.neo4j.fieldaccess.DynamicProperties;

import at.ac.tuwien.dsg.comot.model.ComotDynamicPropertiesContainer;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@RelationshipEntity(type = Change.REL)
public class Change implements Serializable {

	private static final long serialVersionUID = -4184717754543656669L;
	public static final String REL = "CHANGE";

	public static final String DYNAMIC_PROPERTIES = "properties-";

	@XmlTransient
	@GraphId
	protected Long graphId;
	@XmlAttribute
	protected Long timestamp;
	@XmlAttribute
	protected String type;
	@XmlAttribute
	protected String targetObjectId;
	@XmlTransient
	protected DynamicProperties properties; // instead of Map<String, String>

	@XmlIDREF
	@StartNode
	protected Revision from;
	@XmlIDREF
	@EndNode
	protected Revision to;

	public Change() {
	}

	public Change(String type) {
		this();
		this.type = type;
	}

	public Change(Long timestamp, String type, String targetObjectId, Map<String, Object> changeProperties,
			Revision from, Revision to) {
		super();
		this.timestamp = timestamp;
		this.type = type;
		this.from = from;
		this.to = to;
		this.targetObjectId = targetObjectId;
		if (changeProperties != null) {
			setPropertiesMap(changeProperties);
		}
	}

	public void addProperty(String key, Object value) {
		if (properties == null) {
			properties = new ComotDynamicPropertiesContainer();
		}
		properties.setProperty(key, value);
	}

	public Object getProperty(String key) {
		return properties.getProperty(key);
	}

	protected Map<String, Object> convert(DynamicProperties props) {

		// Map<String, Object> map = new HashMap<>();
		// for (String key : props.asMap().keySet()) {
		// map.put(key, props.asMap().get(key));
		// }
		return props.asMap();
	}

	protected DynamicProperties convert(Map<String, Object> map) {

		DynamicProperties properties = new ComotDynamicPropertiesContainer();

		for (String key : map.keySet()) {
			if (map.get(key) != null) {
				properties.setProperty(key, map.get(key));
			}
		}
		// properties.setPropertiesFrom(map);
		return properties;
	}

	@XmlElement
	public Map<String, Object> getPropertiesMap() {
		return convert(properties);
	}

	public void setPropertiesMap(Map<String, Object> properties) {
		this.properties = convert(properties);
	}

	public static String propertyKey(String name) {
		return DYNAMIC_PROPERTIES + name;
	}

	// GENERATED METHODS

	public Long getGraphId() {
		return graphId;
	}

	public void setGraphId(Long graphId) {
		this.graphId = graphId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Revision getFrom() {
		return from;
	}

	public void setFrom(Revision from) {
		this.from = from;
	}

	public Revision getTo() {
		return to;
	}

	public void setTo(Revision to) {
		this.to = to;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public DynamicProperties getProperties() {
		return properties;
	}

	public void setProperties(DynamicProperties properties) {
		this.properties = properties;
	}

	public String getTargetObjectId() {
		return targetObjectId;
	}

	public void setTargetObjectId(String targetObjectId) {
		this.targetObjectId = targetObjectId;
	}

	@Override
	public String toString() {
		return "Change [graphId=" + graphId + ", targetObjectId=" + targetObjectId + ", timestamp=" + timestamp
				+ ", type=" + type + ", from="
				+ ((from == null) ? null : from.getNodeId())
				+ ", to=" + ((to == null) ? null : to.getNodeId()) + "]";
	}

}
