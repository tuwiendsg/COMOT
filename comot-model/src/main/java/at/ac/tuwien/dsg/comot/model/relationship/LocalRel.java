package at.ac.tuwien.dsg.comot.model.relationship;

import at.ac.tuwien.dsg.comot.model.structure.StackNode;

public class LocalRel extends Relationship {

	private static final long serialVersionUID = 7196851315307314559L;

	public LocalRel() {
	}

	public LocalRel(String id, StackNode from, StackNode to) {
		super();
		this.id = id;
		this.from = from;
		this.to = to;
	}

}
