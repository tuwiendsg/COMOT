package at.ac.tuwien.dsg.comot.graph.model.type;

public enum NodeType {

	OS("os"),
	WAR("war"),
	DOCKER("docker"), // dd Software container
	TOMCAT("tomcat"), // dd Software container
	SOFTWARE("software"); // dd Software artifacts

	final String type;

	NodeType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}

	public static NodeType fromString(String type) {
		if (type != null) {
			for (NodeType b : NodeType.values()) {
				if (type.equalsIgnoreCase(b.type)) {
					return b;
				}
			}
		}
		return null;
	}

}
