package at.ac.tuwien.dsg.comot.graph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.neo4j.fieldaccess.DynamicProperties;
import org.springframework.data.neo4j.fieldaccess.DynamicPropertiesContainer;

/**
 * Custom implementation of DynamicProperties, because DinamicPropertiesContainer is not serializable
 * @author Juraj
 *
 */
public class ComotDynamicPropertiesContainer implements DynamicProperties, Serializable{

	private static final long serialVersionUID = 8591988617476025361L;
	
	private final Map<String, Object> map = new HashMap<String, Object>();
    private boolean dirty;

    public ComotDynamicPropertiesContainer() {
		
	}
	
	public ComotDynamicPropertiesContainer(Map<String, Object> map) {
		this.map.putAll(map);
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
		if(!hasProperty(key)) {
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
