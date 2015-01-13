package at.ac.tuwien.dsg.comot.model.node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.fieldaccess.DynamicProperties;

import at.ac.tuwien.dsg.comot.model.ComotDynamicPropertiesContainer;
import at.ac.tuwien.dsg.comot.model.type.NodePropertiesType;
import at.ac.tuwien.dsg.comot.recorder.BusinessId;

@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public class Properties implements Serializable {

	private static final long serialVersionUID = -3590178619365886987L;

	@GraphId
	protected Long nodeId;

	@BusinessId
	@XmlAttribute
	protected String id;
	@XmlAttribute
	protected NodePropertiesType propertiesType;

	@XmlJavaTypeAdapter(ComotDynamicPropertiesContainer.Adapter.class)
	protected DynamicProperties properties; // instead of Map<String, String>

	public Properties() {

	}

	public Properties(NodePropertiesType propertiesType) {
		super();
		this.propertiesType = propertiesType;
	}

	public Properties(String id, NodePropertiesType propertiesType, Map<String, String> map) {
		this(propertiesType);
		this.id = id;
		setPropertiesMap(map);
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
