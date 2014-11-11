package at.ac.tuwien.dsg.comot.common.model.structure;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;

public class CloudService extends ServicePart {

	protected List<ServiceTopology> serviceTopologies = new ArrayList<>();
	protected List<EntityRelationship> relationships = new ArrayList<>();
	protected String accessIp;

	public CloudService() {
	}

	public CloudService(String id) {
		super(id);
	}

	public void addServiceTopology(ServiceTopology serviceTopology) {
		if (serviceTopologies == null) {
			serviceTopologies = new ArrayList<>();
		}
		serviceTopologies.add(serviceTopology);
	}

	public void addEntityRelationship(EntityRelationship relationship) {
		if (relationships == null) {
			relationships = new ArrayList<>();
		}
		relationships.add(relationship);
	}

	public boolean containsRelationship(String newRelId) {
		for (EntityRelationship rel : relationships) {
			if (rel.getId().equals(newRelId)) {
				return true;
			}
		}
		return false;
	}

	// GENERATED METHODS

	public List<ServiceTopology> getServiceTopologies() {
		return serviceTopologies;
	}

	public void setServiceTopologies(List<ServiceTopology> serviceTopologies) {
		this.serviceTopologies = serviceTopologies;
	}

	public List<EntityRelationship> getRelationships() {
		return relationships;
	}

	public void setRelationships(List<EntityRelationship> relationships) {
		this.relationships = relationships;
	}

	public String getAccessIp() {
		return accessIp;
	}

	public void setAccessIp(String accessIp) {
		this.accessIp = accessIp;
	}
	

}
