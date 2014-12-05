package at.ac.tuwien.dsg.comot.graph.model.type;

public enum NodePropertiesType {

	OS("os"),
	ACTION("action");

	final String type;

	NodePropertiesType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}

	public static NodePropertiesType fromString(String type) {
		if (type != null) {
			for (NodePropertiesType b : NodePropertiesType.values()) {
				if (type.equalsIgnoreCase(b.type)) {
					return b;
				}
			}
		}
		return null;
	}

}
