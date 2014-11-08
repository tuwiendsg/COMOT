package at.ac.tuwien.dsg.comot.common.model.unit;

import java.util.HashMap;
import java.util.Map;

import at.ac.tuwien.dsg.comot.common.model.type.ServiceUnitPropertiesType;

public class Properties {

	protected ServiceUnitPropertiesType propertiesType;
	protected Map<String, String> properties = new HashMap<>();

	public Properties() {

	}

	public Properties(ServiceUnitPropertiesType propertiesType) {
		super();
		this.propertiesType = propertiesType;
	}

	public Properties(ServiceUnitPropertiesType propertiesType, Map<String, String> properties) {
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

	public ServiceUnitPropertiesType getPropertiesType() {
		return propertiesType;
	}

	public void setPropertiesType(ServiceUnitPropertiesType propertiesType) {
		this.propertiesType = propertiesType;
	}

}
