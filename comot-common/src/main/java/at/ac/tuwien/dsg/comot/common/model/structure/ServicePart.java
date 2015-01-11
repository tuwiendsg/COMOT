package at.ac.tuwien.dsg.comot.common.model.structure;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.dsg.comot.common.model.AbstractEntity;
import at.ac.tuwien.dsg.comot.common.model.SyblDirective;
import at.ac.tuwien.dsg.comot.common.model.type.State;

public abstract class ServicePart extends AbstractEntity {

	private static final long serialVersionUID = -889982124609754463L;

	protected List<SyblDirective> directives = new ArrayList<>();
	protected State state;

	public ServicePart() {
	}

	public ServicePart(String id) {
		super(id);
	}

	public ServicePart(String id, String name) {
		super(id, name);
	}

	public ServicePart(String id, List<SyblDirective> directives) {
		super(id);
		this.directives = directives;
	}

	public ServicePart(String id, String name, List<SyblDirective> directives) {
		super(id, name);
		this.directives = directives;
	}

	public void addSyblDirective(SyblDirective directive) {
		if (directives == null) {
			directives = new ArrayList<>();
		}
		directives.add(directive);
	}

	// GENERATED METHODS

	public List<SyblDirective> getDirectives() {
		return directives;
	}

	public void setDirectives(List<SyblDirective> directives) {
		this.directives = directives;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

}