package at.ac.tuwien.dsg.comot.common.model.type;

public enum ServiceUnitType {

	OperatingSystem("os"), Software("software");

	final String type;

	ServiceUnitType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}

}
