package at.ac.tuwien.dsg.comot.common.model;

import at.ac.tuwien.dsg.comot.common.model.structure.ServicePart;
import at.ac.tuwien.dsg.comot.common.model.type.RelationshipType;

public class EntityRelationship extends AbstractEntity implements ReferencableEntity {

	protected RelationshipType type;
	protected ReferencableEntity from;
	protected ReferencableEntity to;

	protected ServicePart fromPart;
	protected ServicePart toPart;

	public EntityRelationship() {
	}

	public EntityRelationship(String id, RelationshipType type, ReferencableEntity from, ReferencableEntity to,
			ServicePart fromPart, ServicePart toPart) {
		super(id);
		this.type = type;
		this.from = from;
		this.to = to;
		this.fromPart = fromPart;
		this.toPart = toPart;
	}


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

	public ServicePart getFromPart() {
		return fromPart;
	}

	public void setFromPart(ServicePart fromPart) {
		this.fromPart = fromPart;
	}

	public ServicePart getToPart() {
		return toPart;
	}

	public void setToPart(ServicePart toPart) {
		this.toPart = toPart;
	}

}
