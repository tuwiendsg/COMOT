package at.ac.tuwien.dsg.comot.common.model.type;

// TODO: check with Hung if there are also other types
public enum CapabilityType {
	VARIABLE("variable");

	private final String type;

	CapabilityType() {
		type = null;
	}

	CapabilityType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}

	public static CapabilityType fromString(String type) {
		if (type != null) {
			for (CapabilityType b : CapabilityType.values()) {
				if (type.equalsIgnoreCase(b.type)) {
					return b;
				}
			}
		}
		return null;
	}
}