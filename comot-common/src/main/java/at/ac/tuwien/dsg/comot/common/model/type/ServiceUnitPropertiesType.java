package at.ac.tuwien.dsg.comot.common.model.type;

public enum ServiceUnitPropertiesType {

	OS("os"),
	ACTION("action");

	final String type;

	ServiceUnitPropertiesType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}

	public static ServiceUnitPropertiesType fromString(String type) {
		if (type != null) {
			for (ServiceUnitPropertiesType b : ServiceUnitPropertiesType.values()) {
				if (type.equalsIgnoreCase(b.type)) {
					return b;
				}
			}
		}
		return null;
	}

}
