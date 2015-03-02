package at.ac.tuwien.dsg.comot.m.ui.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "map")
public class JaxbMap<K, V> implements Serializable {

	private static final long serialVersionUID = 3633922400061926553L;

	// @XmlAnyElement(lax = true)
	private Map<K, V> map;

	public JaxbMap() {
		map = new HashMap<K, V>();
	}

	public JaxbMap(Map<K, V> items) {
		this.map = items;
	}

	public Map<K, V> getMap() {
		return map;
	}

	public void setMap(Map<K, V> map) {
		this.map = map;
	}

}
