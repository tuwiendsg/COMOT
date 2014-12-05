package at.ac.tuwien.dsg.comot.graph.model;

import at.ac.tuwien.dsg.comot.graph.model.type.RelationshipType;

public class EntityRelationship extends AbstractEntity {

	private static final long serialVersionUID = -8802319806655668518L;

	protected RelationshipType type;

	protected String from;
	protected String to;

	public EntityRelationship() {
	}

	public EntityRelationship(String id, RelationshipType type, String from, String to) {
		super(id);
		this.type = type;
		this.from = from;
		this.to = to;
	}

	// GENERATED METHODS

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public RelationshipType getType() {
		return type;
	}

	public void setType(RelationshipType type) {
		this.type = type;
	}
}
