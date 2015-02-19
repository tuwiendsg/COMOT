package at.ac.tuwien.dsg.comot.model.provider;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.fieldaccess.DynamicProperties;

import at.ac.tuwien.dsg.comot.model.ComotDynamicPropertiesContainer;
import at.ac.tuwien.dsg.comot.model.HasUniqueId;
import at.ac.tuwien.dsg.comot.model.type.OsuType;
import at.ac.tuwien.dsg.comot.recorder.BusinessId;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public class OfferedServiceUnit implements HasUniqueId, Serializable {

	private static final long serialVersionUID = 4681582673568700847L;

	@GraphId
	protected Long nodeId;
	@BusinessId
	@XmlID
	@XmlAttribute
	protected String id;
	@XmlAttribute
	protected OsuType type;
	@XmlJavaTypeAdapter(ComotDynamicPropertiesContainer.Adapter.class)
	protected DynamicProperties resources;
	@XmlElementWrapper(name = "PrimitiveOperations")
	@XmlElement(name = "Operation")
	protected Set<PrimitiveOperation> operations = new HashSet<>();

	public OfferedServiceUnit() {
	}

	public OfferedServiceUnit(String id, OsuType type) {
		this.id = id;
		this.type = type;
	}

	public void addResource(String key, String value) {
		if (resources == null) {
			resources = new ComotDynamicPropertiesContainer();
		}
		resources.setProperty(key, value);
	}
	
	public void addOperation(PrimitiveOperation operation) {
		if (operations == null) {
			operations = new HashSet<>();
		}
		operations.add(operation);
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

	public Map<String, String> getResources() {
		return convert(resources);
	}

	public void setResources(Map<String, String> properties) {
		this.resources = convert(properties);
	}

	// GENERATED METHODS

	public OsuType getType() {
		return type;
	}

	public void setType(OsuType type) {
		this.type = type;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


	public Set<PrimitiveOperation> getOperations() {
		return operations;
	}

	public void setOperations(Set<PrimitiveOperation> operations) {
		this.operations = operations;
	}

	public void setResources(DynamicProperties resources) {
		this.resources = resources;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OfferedServiceUnit other = (OfferedServiceUnit) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
