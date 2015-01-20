package at.ac.tuwien.dsg.comot.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.springframework.data.neo4j.fieldaccess.DynamicProperties;
import org.springframework.data.neo4j.fieldaccess.DynamicPropertiesContainer;

import at.ac.tuwien.dsg.comot.model.jaxb.XmlStringMapAdapter;

/**
 * Custom implementation of DynamicProperties, because DinamicPropertiesContainer is not serializable
 * 
 * @author Juraj
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ComotDynamicPropertiesContainer implements DynamicProperties, Serializable {

	private static final long serialVersionUID = 8591988617476025361L;
	@XmlJavaTypeAdapter(XmlStringMapAdapter.class)
	private final Map<String, Object> map = new HashMap<String, Object>();
	@XmlTransient
	private boolean dirty;

	public ComotDynamicPropertiesContainer() {

	}

	public ComotDynamicPropertiesContainer(Map<String, Object> map) {
		this.map.putAll(map);
	}

	public static class Adapter extends XmlAdapter<ComotDynamicPropertiesContainer, DynamicProperties> {
		public DynamicProperties unmarshal(ComotDynamicPropertiesContainer v) {
			return v;
		}

		public ComotDynamicPropertiesContainer marshal(DynamicProperties v) {
			return (ComotDynamicPropertiesContainer) v;
		}
	}

	@Override
	public boolean hasProperty(String key) {
		return map.containsKey(key);
	}

	@Override
	public Object getProperty(String key) {
		return map.get(key);
	}

	@Override
	public Object getProperty(String key, Object defaultValue) {
		if (!hasProperty(key)) {
			return defaultValue;
		}
		return getProperty(key);
	}

	@Override
	public void setProperty(String key, Object value) {
		map.put(key, value);
	}

	@Override
	public Object removeProperty(String key) {
		return map.remove(key);
	}

	@Override
	public Iterable<String> getPropertyKeys() {
		return map.keySet();
	}

	@Override
	public Map<String, Object> asMap() {
		return new HashMap<String, Object>(map);
	}

	@Override
	public void setPropertiesFrom(Map<String, Object> m) {
		map.clear();
		map.putAll(m);
		setDirty(true);
	}

	@Override
	public DynamicProperties createFrom(Map<String, Object> map) {
		return new DynamicPropertiesContainer(map);
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public Map<String, Object> getMap() {
		return map;
	}

}
