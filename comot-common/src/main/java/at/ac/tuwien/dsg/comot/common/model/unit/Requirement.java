package at.ac.tuwien.dsg.comot.common.model.unit;

import at.ac.tuwien.dsg.comot.common.model.AbstractEntity;
import at.ac.tuwien.dsg.comot.common.model.ReferencableEntity;
import at.ac.tuwien.dsg.comot.common.model.type.RequirementType;

/**
 * @author omoser
 */
public class Requirement extends AbstractEntity implements ReferencableEntity {

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
