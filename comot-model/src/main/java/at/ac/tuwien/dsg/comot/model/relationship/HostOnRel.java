package at.ac.tuwien.dsg.comot.model.relationship;

import at.ac.tuwien.dsg.comot.model.structure.StackNode;

public class HostOnRel extends Relationship {

	private static final long serialVersionUID = 5995750452255103644L;

	public HostOnRel() {
	}

	public HostOnRel(String id, StackNode from, StackNode to) {
		super();
		this.id = id;
		this.from = from;
		this.to = to;
	}

}
