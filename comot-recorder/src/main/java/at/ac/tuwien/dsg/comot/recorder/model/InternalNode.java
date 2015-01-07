package at.ac.tuwien.dsg.comot.recorder.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InternalNode {

	public static final String ID = "_id";
	public static final String LABEL_STATE_SUFFIX = "_State";

	private String label;
	private String businessId;

	private Map<String, Object> properties = new HashMap<>();
	private Set<InternalRel> relationships = new HashSet<>();

	public InternalNode() {

	}

	public Collection<String> getLablesForIdentityNode() {
		List<String> list = new ArrayList<>();
		list.add(label);
		list.add(LabelTypes._IDENTITY.toString());
		return list;
	}

	public Collection<String> getLablesForStateNode() {
		List<String> list = new ArrayList<>();
		list.add(label + LABEL_STATE_SUFFIX);
		list.add(LabelTypes._STATE.toString());
		return list;
	}

	public Map<String, Object> getBusinessIdAsMap() {
		Map<String, Object> map = new HashMap<>();
		map.put(ID, businessId);
		return map;
	}

	// GENERATED METHODS

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getBusinessId() {
		return businessId;
	}

	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public Set<InternalRel> getRelationships() {
		return relationships;
	}

	public void setRelationships(Set<InternalRel> relationships) {
		this.relationships = relationships;
	}

}
