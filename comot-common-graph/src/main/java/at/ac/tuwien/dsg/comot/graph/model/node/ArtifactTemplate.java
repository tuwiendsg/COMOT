package at.ac.tuwien.dsg.comot.graph.model.node;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.comot.graph.BusinessId;
import at.ac.tuwien.dsg.comot.graph.model.type.ArtifactType;

@NodeEntity
public class ArtifactTemplate implements Serializable {

	private static final long serialVersionUID = 4681582673568700847L;

	@GraphId
	protected Long nodeId;
	@BusinessId
	protected String id;
	protected ArtifactType type;
	protected Set<ArtifactReference> artifactReferences = new HashSet<>();

	public ArtifactTemplate() {
	}

	public ArtifactTemplate(String id, ArtifactType type) {
		this.id = id;
		this.type = type;
	}

	public void addArtifactReference(ArtifactReference reference) {
		if (artifactReferences == null) {
			artifactReferences = new HashSet<ArtifactReference>();
		}
		artifactReferences.add(reference);
	}

	// GENERATED METHODS

	public Set<ArtifactReference> getArtifactReferences() {
		return artifactReferences;
	}

	public void setArtifactReferences(Set<ArtifactReference> artifactReferences) {
		this.artifactReferences = artifactReferences;
	}

	public ArtifactType getType() {
		return type;
	}

	public void setType(ArtifactType type) {
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

}
