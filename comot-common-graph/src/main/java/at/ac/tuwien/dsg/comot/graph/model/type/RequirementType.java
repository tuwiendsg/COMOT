package at.ac.tuwien.dsg.comot.graph.model.type;

public enum RequirementType {

	VARIABLE("variable");

	private final String type;

	RequirementType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}

	public static RequirementType fromString(String type) {
		if (type != null) {
			for (RequirementType b : RequirementType.values()) {
				if (type.equalsIgnoreCase(b.type)) {
					return b;
				}
			}
		}
		return null;
	}
}