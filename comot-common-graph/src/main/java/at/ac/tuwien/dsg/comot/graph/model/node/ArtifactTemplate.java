package at.ac.tuwien.dsg.comot.graph.model.node;

import java.util.HashSet;
import java.util.Set;

import at.ac.tuwien.dsg.comot.graph.model.AbstractEntity;
import at.ac.tuwien.dsg.comot.graph.model.type.ArtifactType;

public class ArtifactTemplate extends AbstractEntity {

	private static final long serialVersionUID = 4681582673568700847L;

	protected ArtifactType type;
	protected Set<ArtifactReference> artifactReferences = new HashSet<>();

	// protected BundleConfig bundleConfig;

	public ArtifactTemplate() {
	}

	public ArtifactTemplate(String id, ArtifactType type) {
		super(id);
		this.type = type;
	}

	public void addArtifactReference(ArtifactReference reference) {
		if (artifactReferences == null) {
			artifactReferences = new HashSet<ArtifactReference>();
		}
		artifactReferences.add(reference);
	}

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

}
