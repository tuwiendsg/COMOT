package at.ac.tuwien.dsg.comot.model.type;

public enum OsuType {
        IaaS("IaaS"),
	OS("os"),
	WAR("war"),
	DOCKER("docker"), // dd Software container
	TOMCAT("tomcat"), // dd Software container
	SOFTWARE("software"); // dd Software artifacts

	final String type;

	OsuType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}

	public static OsuType fromString(String type) {
		if (type != null) {
			for (OsuType b : OsuType.values()) {
				if (type.equalsIgnoreCase(b.type)) {
					return b;
				}
			}
		}
		return null;
	}

}
