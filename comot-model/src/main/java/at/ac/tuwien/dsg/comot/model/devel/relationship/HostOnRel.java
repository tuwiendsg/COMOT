package at.ac.tuwien.dsg.comot.model.devel.relationship;

import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;

public class HostOnRel extends Relationship {

	private static final long serialVersionUID = 5995750452255103644L;

	public HostOnRel() {
	}

	public HostOnRel(String id, ServiceUnit from, ServiceUnit to) {
		super();
		this.id = id;
		this.from = from;
		this.to = to;
	}

}
