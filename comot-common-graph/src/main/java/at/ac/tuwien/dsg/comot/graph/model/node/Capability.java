package at.ac.tuwien.dsg.comot.graph.model.node;

import at.ac.tuwien.dsg.comot.graph.model.AbstractEntity;
import at.ac.tuwien.dsg.comot.graph.model.type.CapabilityType;

public class Capability extends AbstractEntity {

	private static final long serialVersionUID = -573066037034691710L;

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
