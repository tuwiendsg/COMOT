package at.ac.tuwien.dsg.comot.common.model.structure;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;

public class CloudService extends ServicePart {

	protected List<ServiceTopology> serviceTopologies = new ArrayList<>();
	protected List<EntityRelationship> relationships = new ArrayList<>();

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((relationships == null) ? 0 : relationships.hashCode());
		result = prime * result + ((serviceTopologies == null) ? 0 : serviceTopologies.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CloudService other = (CloudService) obj;
		if (relationships == null) {
			if (other.relationships != null)
				return false;
		} else if (!relationships.equals(other.relationships))
			return false;
		if (serviceTopologies == null) {
			if (other.serviceTopologies != null)
				return false;
		} else if (!serviceTopologies.equals(other.serviceTopologies))
			return false;
		return true;
	}

}
