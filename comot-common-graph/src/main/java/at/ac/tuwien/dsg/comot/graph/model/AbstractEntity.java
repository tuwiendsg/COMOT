package at.ac.tuwien.dsg.comot.graph.model;

import java.io.Serializable;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public abstract class AbstractEntity implements Serializable {

	private static final long serialVersionUID = -3579076231648719838L;

	@GraphId
	protected Long nodeId;

	protected String id;
	protected String name;
	protected String description;

	public AbstractEntity() {
	}

	public AbstractEntity(String id) {
		this.id = id;
	}

	public AbstractEntity(String id, String name) {
		this.id = id;
		this.name = name;
	}

	// GENERATED METHODS

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

}
