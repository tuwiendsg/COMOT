package at.ac.tuwien.dsg.comot.graph.model.node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.fieldaccess.DynamicProperties;
import org.springframework.data.neo4j.fieldaccess.DynamicPropertiesContainer;

import at.ac.tuwien.dsg.comot.graph.BusinessId;
import at.ac.tuwien.dsg.comot.graph.model.type.NodePropertiesType;

@NodeEntity
public class Properties implements Serializable {

	private static final long serialVersionUID = -3590178619365886987L;

	@GraphId
	protected Long nodeId;

	@BusinessId
	protected String id;

	protected NodePropertiesType propertiesType;
	protected DynamicProperties properties; // instead of Map<String, String>

	public Properties() {

	}

	public Properties(NodePropertiesType propertiesType) {
		super();
		this.propertiesType = propertiesType;
	}

	public Properties(NodePropertiesType propertiesType, Map<String, String> properties) {
		super();
		this.propertiesType = propertiesType;
		this.properties = convert(properties);
	}

	public void addProperty(String key, String value) {
		if (properties == null) {
			properties = new DynamicPropertiesContainer();
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

		DynamicProperties properties = new DynamicPropertiesContainer();

		for (String key : map.keySet()) {
			properties.setProperty(key, map.get(key));

		}
		return properties;
	}

	public Map<String, String> getPropertiesMap() {
		return convert(properties);
	}

	public void setPropertiesMap(Map<String, String> properties) {
		this.properties = convert(properties);
	}

	// GENERATED METHODS

	public NodePropertiesType getPropertiesType() {
		return propertiesType;
	}

	public void setPropertiesType(NodePropertiesType propertiesType) {
		this.propertiesType = propertiesType;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public DynamicProperties getProperties() {
		return properties;
	}

	public void setProperties(DynamicProperties properties) {
		this.properties = properties;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
