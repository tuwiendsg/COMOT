package at.ac.tuwien.dsg.comot.common.model.type;

public enum ArtifactType {
	Script("sh");

	private final String type;

	ArtifactType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}
}