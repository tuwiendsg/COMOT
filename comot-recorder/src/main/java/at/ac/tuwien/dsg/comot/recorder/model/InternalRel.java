package at.ac.tuwien.dsg.comot.recorder.model;

import java.util.HashMap;
import java.util.Map;

public class InternalRel {

	public static final String PROPERTY_FROM = "from";
	public static final String PROPERTY_TO = "to";

	protected String type;
	protected InternalNode startNode;
	protected InternalNode endNode;

	private Map<String, Object> properties = new HashMap<>();

	public InternalRel() {

	}

	public InternalRel(String type, InternalNode startNode, InternalNode endNode, Map<String, Object> properties) {
		super();
		this.type = type;
		this.startNode = startNode;
		this.endNode = endNode;
		this.properties = properties;
	}

	// GENERATED METHODS

	@Override
	public String toString() {
		return " (" + startNode.getBusinessId() + ") - [" + type + "] -> (" + endNode.getBusinessId() + ") props="
				+ properties;
	}

	public InternalNode getStartNode() {
		return startNode;
	}

	public void setStartNode(InternalNode startNode) {
		this.startNode = startNode;
	}

	public InternalNode getEndNode() {
		return endNode;
	}

	public void setEndNode(InternalNode endNode) {
		this.endNode = endNode;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
