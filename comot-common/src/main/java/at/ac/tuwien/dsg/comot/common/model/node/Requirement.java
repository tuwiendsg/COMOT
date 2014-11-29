package at.ac.tuwien.dsg.comot.common.model.node;

import at.ac.tuwien.dsg.comot.common.model.AbstractEntity;
import at.ac.tuwien.dsg.comot.common.model.type.RequirementType;

public class Requirement extends AbstractEntity {

	private static final long serialVersionUID = -6524058426472480121L;

	protected RequirementType type;

	public Requirement() {
	}

	public Requirement(String id, RequirementType type) {
		super(id);
		this.type = type;
	}

	public RequirementType getType() {
		return type;
	}

	public void setType(RequirementType type) {
		this.type = type;
	}

}
