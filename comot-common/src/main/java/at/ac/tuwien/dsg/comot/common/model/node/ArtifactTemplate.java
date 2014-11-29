package at.ac.tuwien.dsg.comot.common.model.node;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.dsg.comot.common.model.AbstractEntity;
import at.ac.tuwien.dsg.comot.common.model.type.ArtifactType;

public class ArtifactTemplate extends AbstractEntity {

	private static final long serialVersionUID = 4681582673568700847L;

	protected ArtifactType type;
	protected List<ArtifactReference> artifactReferences = new ArrayList<>();

	// protected BundleConfig bundleConfig;

	public ArtifactTemplate() {
	}

	public ArtifactTemplate(String id, ArtifactType type) {
		super(id);
		this.type = type;
	}

	public void addArtifactReference(ArtifactReference reference) {
		if (artifactReferences == null) {
			artifactReferences = new ArrayList<ArtifactReference>();
		}
		artifactReferences.add(reference);
	}

	public List<ArtifactReference> getArtifactReferences() {
		return artifactReferences;
	}

	public void setArtifactReferences(List<ArtifactReference> artifactReferences) {
		this.artifactReferences = artifactReferences;
	}

	public ArtifactType getType() {
		return type;
	}

	public void setType(ArtifactType type) {
		this.type = type;
	}

}
