package at.ac.tuwien.dsg.comot.graph.model.node;

import java.io.Serializable;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class ArtifactReference implements Serializable {

	private static final long serialVersionUID = 4037194207570549282L;

	@GraphId
	protected Long nodeId;

	protected String id;
	protected String uri;

	public ArtifactReference() {
	}

	public ArtifactReference(String id) {
		this.id = id;
	}

	public ArtifactReference(String id, String uri) {
		this.id = id;
		this.uri = uri;
	}

	// GENERATED METHODS

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	@Override
	public String toString() {
		return uri;
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

}
