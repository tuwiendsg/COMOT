package at.ac.tuwien.dsg.comot.recorder.model;

import java.util.HashMap;
import java.util.Map;

public class InternalRel {

	public static final String PROPERTY_FROM = "from";
	public static final String PROPERTY_TO = "to";

	protected String type;
	protected InternalNode startNode;
	protected InternalNode endNode;

	protected Long from;
	protected Long to;

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

	public Long getFrom() {
		return from;
	}

	public void setFrom(Long from) {
		this.from = from;
	}

	public Long getTo() {
		return to;
	}

	public void setTo(Long to) {
		this.to = to;
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
