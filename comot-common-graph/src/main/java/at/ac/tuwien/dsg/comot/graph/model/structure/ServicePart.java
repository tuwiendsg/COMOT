package at.ac.tuwien.dsg.comot.graph.model.structure;

import java.util.HashSet;
import java.util.Set;

import at.ac.tuwien.dsg.comot.graph.model.AbstractEntity;
import at.ac.tuwien.dsg.comot.graph.model.SyblDirective;
import at.ac.tuwien.dsg.comot.graph.model.type.State;


public abstract class ServicePart extends AbstractEntity {

	private static final long serialVersionUID = -889982124609754463L;

	protected Set<SyblDirective> directives = new HashSet<>();
	protected State state;

	public ServicePart() {
	}

	public ServicePart(String id) {
		super(id);
	}

	public ServicePart(String id, String name) {
		super(id, name);
	}

	public ServicePart(String id, Set<SyblDirective> directives) {
		super(id);
		this.directives = directives;
	}

	public ServicePart(String id, String name, Set<SyblDirective> directives) {
		super(id, name);
		this.directives = directives;
	}

	public void addSyblDirective(SyblDirective directive) {
		if (directives == null) {
			directives = new HashSet<>();
		}
		directives.add(directive);
	}

	// GENERATED METHODS

	public Set<SyblDirective> getDirectives() {
		return directives;
	}

	public void setDirectives(Set<SyblDirective> directives) {
		this.directives = directives;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

}
