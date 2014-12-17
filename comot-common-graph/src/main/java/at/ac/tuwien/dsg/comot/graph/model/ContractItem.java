package at.ac.tuwien.dsg.comot.graph.model;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class ContractItem {

	@GraphId
	protected Long nodeId;
	protected Set<XSelectable> properties = new HashSet<>();
	
	public ContractItem(){
		
	}
	
	public void addProperty(XSelectable object) {
		if (properties == null) {
			properties = new HashSet<>();
		}
		properties.add(object);
	}


	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public Set<XSelectable> getProperties() {
		return properties;
	}

	public void setProperties(Set<XSelectable> properties) {
		this.properties = properties;
	}

}
