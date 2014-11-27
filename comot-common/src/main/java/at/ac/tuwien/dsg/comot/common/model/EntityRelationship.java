package at.ac.tuwien.dsg.comot.common.model;

import at.ac.tuwien.dsg.comot.common.model.type.RelationshipType;

public class EntityRelationship extends AbstractEntity implements ReferencableEntity {

	private static final long serialVersionUID = -8802319806655668518L;

	protected RelationshipType type;

	protected ReferencableEntity from;
	protected ReferencableEntity to;

	// protected ServicePart fromPart;
	// protected ServicePart toPart;

	public EntityRelationship() {
	}

	public EntityRelationship(String id, RelationshipType type, ReferencableEntity from, ReferencableEntity to
	// ,
	// ServicePart fromPart, ServicePart toPart
	) {
		super(id);
		this.type = type;
		this.from = from;
		this.to = to;
		// this.fromPart = fromPart;
		// this.toPart = toPart;
	}

	// public boolean isServicePartRelationship() {
	// if (fromPart != null && toPart != null) {
	// return true;
	// } else {
	// return false;
	// }
	// }

	// GENERATED METHODS

	public ReferencableEntity getTo() {
		return to;
	}

	public void setTo(ReferencableEntity to) {
		this.to = to;
	}

	public ReferencableEntity getFrom() {
		return from;
	}

	public void setFrom(ReferencableEntity from) {
		this.from = from;
	}

	public RelationshipType getType() {
		return type;
	}

	public void setType(RelationshipType type) {
		this.type = type;
	}
}
