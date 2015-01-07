package at.ac.tuwien.dsg.comot.graph.model;

import java.io.Serializable;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.comot.graph.BusinessId;
import at.ac.tuwien.dsg.comot.graph.model.type.DirectiveType;

@NodeEntity
public class SyblDirective implements Serializable {

	private static final long serialVersionUID = 1938899881721566640L;

	@GraphId
	protected Long nodeId;

	@BusinessId
	protected String id;

	protected String directive;
	protected DirectiveType type;

	public SyblDirective() {
	}

	public SyblDirective(String id, DirectiveType type, String directive) {
		this.id = id;
		this.directive = directive;
		this.type = type;
	}

	public String getDirective() {
		return directive;
	}

	public void setDirective(String directive) {
		this.directive = directive;
	}

	public DirectiveType getType() {
		return type;
	}

	public void setType(DirectiveType type) {
		this.type = type;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
