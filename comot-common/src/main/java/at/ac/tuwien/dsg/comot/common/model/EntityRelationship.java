package at.ac.tuwien.dsg.comot.common.model;

import at.ac.tuwien.dsg.comot.common.model.structure.ServicePart;

public class EntityRelationship extends AbstractEntity {

	protected ServicePart from;
	protected ServicePart to;

	public EntityRelationship() {
	}

	public EntityRelationship(String id) {
		super(id);
	}

	public EntityRelationship(String id, String type, ServicePart from, ServicePart to) {
		super(id, type);
		this.from = from;
		this.to = to;
	}

	public ServicePart getFrom() {
		return from;
	}

	public void setFrom(ServicePart from) {
		this.from = from;
	}

	public ServicePart getTo() {
		return to;
	}

	public void setTo(ServicePart to) {
		this.to = to;
	}

}
