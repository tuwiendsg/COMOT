package at.ac.tuwien.dsg.comot.common.model.elastic;

import at.ac.tuwien.dsg.comot.common.model.AbstractEntity;

public class Capability extends AbstractEntity {

	public Capability() {
	}

	public Capability(String id, String type) {
		super(id);
		setType(type);
	}

	@Override
	public String toString() {
		return "Capability [id=" + id + ", description=" + description + ", type=" + type + ", name=" + name + "]";
	}

}
