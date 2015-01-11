package at.ac.tuwien.dsg.comot.recorder.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ManagedRegion {

	public static final String LABEL_REGION = "_REGION";
	public static final String PROP_ID = "_id";
	public static final String PROP_TIMESTAMP = "_timestamp";

	protected Set<InternalNode> nodes;
	protected Set<InternalRel> relationships;
	protected Map<String, String> classes = new HashMap<>();
	protected InternalNode startNode;

	public ManagedRegion() {

	}

	public ManagedRegion(InternalNode startNode) {
		super();
		this.startNode = startNode;
	}

	public void addClass(String key, String value) {
		if (classes == null) {
			classes = new HashMap<>();
		}
		classes.put(key, value);
	}

	public void addNode(InternalNode node) {
		if (nodes == null) {
			nodes = new HashSet<>();
		}
		nodes.add(node);
	}

	public void addRelationship(InternalRel rel) {
		if (relationships == null) {
			relationships = new HashSet<>();
		}
		relationships.add(rel);
	}

	// GENERATED METHODS

	public Set<InternalNode> getNodes() {
		return nodes;
	}

	public void setNodes(Set<InternalNode> nodes) {
		this.nodes = nodes;
	}

	public Set<InternalRel> getRelationships() {
		return relationships;
	}

	public void setRelationships(Set<InternalRel> relationships) {
		this.relationships = relationships;
	}

	public Map<String, String> getClasses() {
		return classes;
	}

	public void setClasses(Map<String, String> classes) {
		this.classes = classes;
	}

	public InternalNode getStartNode() {
		return startNode;
	}

	public void setStartNode(InternalNode startNode) {
		this.startNode = startNode;
	}

}
