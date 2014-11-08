package at.ac.tuwien.dsg.comot.common.model;

import at.ac.tuwien.dsg.comot.common.model.type.RelationshipType;

public class EntityRelationship extends AbstractEntity implements ReferencableEntity {

	protected RelationshipType type;
	protected ReferencableEntity from;
	protected ReferencableEntity to;

	public EntityRelationship() {
	}

	public EntityRelationship(String id, RelationshipType type, ReferencableEntity from, ReferencableEntity to) {
		super(id);
		this.from = from;
		this.to = to;
		this.type = type;
	}

	public ReferencableEntity getFrom() {
		return from;
	}

	public void setFrom(ReferencableEntity from) {
		this.from = from;
	}

	public ReferencableEntity getTo() {
		return to;
	}

	public void setTo(ReferencableEntity to) {
		this.to = to;
	}

	public RelationshipType getType() {
		return type;
	}

	public void setType(RelationshipType type) {
		this.type = type;
	}

}
