package at.ac.tuwien.dsg.comot.model.type;

public enum ResourceType {
	SCRIPT("sh"),
	WAR_FILE("war"),
	CHEF("chef"),
	CHEF_SOLO("chef-solo"),
	APT_GET_COMMAND("apt"),
	//
	INSTANCE_TYPE("instanceType"),
	PROVIDER("provider"),
	IMAGE("baseImage"),
	PACKAGES("packages");

	private final String type;

	ResourceType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}

	public static ResourceType fromString(String type) {
		if (type != null) {
			for (ResourceType b : ResourceType.values()) {
				if (type.equalsIgnoreCase(b.type)) {
					return b;
				}
			}
		}
		return null;
	}
}