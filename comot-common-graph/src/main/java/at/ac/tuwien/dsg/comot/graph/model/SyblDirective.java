package at.ac.tuwien.dsg.comot.graph.model;

import at.ac.tuwien.dsg.comot.graph.model.type.DirectiveType;

public class SyblDirective extends AbstractEntity {

	private static final long serialVersionUID = 1938899881721566640L;

	protected String directive;
	protected DirectiveType type;

	public SyblDirective() {
	}

	public SyblDirective(String id) {
		super(id);
	}

	public SyblDirective(String id, DirectiveType type, String directive) {
		super(id);
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

}
