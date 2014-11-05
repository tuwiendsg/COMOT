package at.ac.tuwien.dsg.comot.common.model.type;

public enum CapabilityType {
	Variable("variable");

	private final String type;

	CapabilityType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}
}