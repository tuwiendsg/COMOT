package at.ac.tuwien.dsg.comot.common.model.unit;

import at.ac.tuwien.dsg.comot.common.model.AbstractEntity;
import at.ac.tuwien.dsg.comot.common.model.ReferencableEntity;
import at.ac.tuwien.dsg.comot.common.model.type.CapabilityType;

public class Capability extends AbstractEntity implements ReferencableEntity {

	protected CapabilityType type;

	public Capability() {
	}

	public Capability(String id, CapabilityType type) {
		super(id);
		this.type = type;
	}

	public CapabilityType getType() {
		return type;
	}

	public void setType(CapabilityType type) {
		this.type = type;
	}

}
