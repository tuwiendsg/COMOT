package at.ac.tuwien.dsg.comot.common.model.type;

public enum ServiceUnitType {

	OS("os"),
	DOCKER("docker"),
	TOMCAT("tomcat"),
	SOFTWARE("software");

	final String type;

	ServiceUnitType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}

	public static ServiceUnitType fromString(String type) {
		if (type != null) {
			for (ServiceUnitType b : ServiceUnitType.values()) {
				if (type.equalsIgnoreCase(b.type)) {
					return b;
				}
			}
		}
		return null;
	}

}
