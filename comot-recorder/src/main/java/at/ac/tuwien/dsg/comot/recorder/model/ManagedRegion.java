package at.ac.tuwien.dsg.comot.recorder.model;

import java.util.HashSet;
import java.util.Set;

public class ManagedRegion {

	public static final String LABEL_REGION = "_REGION";
	public static final String PROPERTY_ID = "_id";

	protected Set<InternalNode> nodes;
	protected Set<InternalRel> relationships;

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

}
