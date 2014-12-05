package at.ac.tuwien.dsg.comot.graph.model.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Transient;

import at.ac.tuwien.dsg.comot.graph.model.EntityRelationship;

public class CloudService extends ServicePart {

	private static final long serialVersionUID = 4336434946064439267L;
	
	protected Set<ServiceTopology> serviceTopologies = new HashSet<>();
	@Transient
	protected List<EntityRelationship> relationships = new ArrayList<>();

	protected String accessIp;

	public CloudService() {
	}

	public CloudService(String id) {
		super(id);
	}

	public void addServiceTopology(ServiceTopology serviceTopology) {
		if (serviceTopologies == null) {
			serviceTopologies = new HashSet<>();
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

	public Set<ServiceTopology> getServiceTopologies() {
		return serviceTopologies;
	}

	public void setServiceTopologies(Set<ServiceTopology> serviceTopologies) {
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
