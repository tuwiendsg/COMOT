package at.ac.tuwien.dsg.comot.common.model.node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import at.ac.tuwien.dsg.comot.common.model.type.NodePropertiesType;

public class Properties implements Serializable {

	private static final long serialVersionUID = -3590178619365886987L;

	protected NodePropertiesType propertiesType;
	protected Map<String, String> properties = new HashMap<>();

	public Properties() {

	}

	public Properties(NodePropertiesType propertiesType) {
		super();
		this.propertiesType = propertiesType;
	}

	public Properties(NodePropertiesType propertiesType, Map<String, String> properties) {
		super();
		this.propertiesType = propertiesType;
		this.properties = properties;
	}

	public void addProperty(String key, String value) {
		if (properties == null) {
			properties = new HashMap<>();
		}
		properties.put(key, value);
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public NodePropertiesType getPropertiesType() {
		return propertiesType;
	}

	public void setPropertiesType(NodePropertiesType propertiesType) {
		this.propertiesType = propertiesType;
	}

}
