package at.ac.tuwien.dsg.comot.graph.model.structure;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.comot.graph.model.XSelectable;
import at.ac.tuwien.dsg.comot.graph.model.SyblDirective;
import at.ac.tuwien.dsg.comot.graph.model.type.State;

@NodeEntity
public abstract class ServicePart implements Serializable, XSelectable {

	private static final long serialVersionUID = -889982124609754463L;

	@GraphId
	protected Long nodeId;

	protected String id;
	protected String name;
	protected State state;

	protected Set<SyblDirective> directives = new HashSet<>();

	public ServicePart() {
	}

	public ServicePart(String id) {
		this.id = id;
	}

	public ServicePart(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public ServicePart(String id, Set<SyblDirective> directives) {
		this.id = id;
		this.directives = directives;
	}

	public ServicePart(String id, String name, Set<SyblDirective> directives) {
		this(id, name);
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

}
