package at.ac.tuwien.dsg.comot.common.model.type;

public enum RelationshipType {

	ConnectedTo("CONNECTTO"),
	HostedOn("HOSTON");

	private String type;

	RelationshipType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return type;
	}
}