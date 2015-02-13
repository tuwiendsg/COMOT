package at.ac.tuwien.dsg.comot.model.type;

public enum ArtifactType {
	SCRIPT("sh"),
	WAR_FILE("war"),
	CHEF("chef"),
	CHEF_SOLO("chef-solo"),
	APT_GET_COMMAND("apt");

	private final String type;

	ArtifactType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}

	public static ArtifactType fromString(String type) {
		if (type != null) {
			for (ArtifactType b : ArtifactType.values()) {
				if (type.equalsIgnoreCase(b.type)) {
					return b;
				}
			}
		}
		return null;
	}
}