package at.ac.tuwien.dsg.comot.m.recorder.model;

import java.io.Serializable;
import java.util.HashMap;
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

	@XmlTransient
	@GraphId
	protected Long graphId;
	@XmlAttribute
	protected Long timestamp;
	@XmlAttribute
	protected String type;
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

	public Change(Long timestamp, String type, Map<String, String> changeProperties, Revision from, Revision to) {
		super();
		this.timestamp = timestamp;
		this.type = type;
		this.from = from;
		this.to = to;
		if (changeProperties != null) {
			setPropertiesMap(changeProperties);
		}
	}

	public void addProperty(String key, String value) {
		if (properties == null) {
			properties = new ComotDynamicPropertiesContainer();
		}
		properties.setProperty(key, value);
	}

	protected Map<String, String> convert(DynamicProperties props) {

		Map<String, String> map = new HashMap<>();
		for (String key : props.asMap().keySet()) {
			map.put(key, (String) props.asMap().get(key));
		}
		return map;
	}

	protected DynamicProperties convert(Map<String, String> map) {

		DynamicProperties properties = new ComotDynamicPropertiesContainer();

		for (String key : map.keySet()) {
			properties.setProperty(key, map.get(key));

		}
		return properties;
	}

	@XmlElement
	public Map<String, String> getPropertiesMap() {
		return convert(properties);
	}

	@XmlElement
	public void setPropertiesMap(Map<String, String> properties) {
		this.properties = convert(properties);
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

	@Override
	public String toString() {
		return "Change [graphId=" + graphId + ", timestamp=" + timestamp + ", type=" + type + ", from="
				+ ((from == null) ? null : from.getNodeId())
				+ ", to=" + ((to == null) ? null : to.getNodeId()) + "]";
	}

}
