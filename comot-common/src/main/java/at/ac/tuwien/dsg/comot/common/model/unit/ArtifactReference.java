package at.ac.tuwien.dsg.comot.common.model.unit;

import at.ac.tuwien.dsg.comot.common.model.AbstractEntity;

public class ArtifactReference extends AbstractEntity {

	protected String uri;

	public ArtifactReference() {
	}

	public ArtifactReference(String id) {
		super(id);
	}

	public ArtifactReference(String id, String uri) {
		super(id);
		this.uri = uri;
	}

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

}
