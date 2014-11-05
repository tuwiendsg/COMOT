package at.ac.tuwien.dsg.comot.common.model;

import java.util.ArrayList;
import java.util.List;

public class ArtifactTemplate extends AbstractEntity {

	protected List<ArtifactReference> artifactReferences = new ArrayList<>();

	// protected BundleConfig bundleConfig;

	public ArtifactTemplate() {
	}

	public ArtifactTemplate(String id) {
		super(id);
	}

	public ArtifactTemplate(String id, String type) {
		super(id);
		this.setType(type);
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

}
