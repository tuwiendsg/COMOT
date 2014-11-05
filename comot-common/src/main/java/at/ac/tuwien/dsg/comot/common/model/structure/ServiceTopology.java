package at.ac.tuwien.dsg.comot.common.model.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.comot.common.model.elastic.Capability;
import at.ac.tuwien.dsg.comot.common.model.elastic.ElasticityCapability;
import at.ac.tuwien.dsg.comot.common.model.elastic.Requirement;
import at.ac.tuwien.dsg.comot.common.model.elastic.SyblDirective;

public class ServiceTopology extends ServicePart {

	protected List<ServiceUnit> serviceUnits = new ArrayList<>();
	protected List<ServiceTopology> serviceTopologies = new ArrayList<>();

	public ServiceTopology() {
	}

	public ServiceTopology(String id) {
		super(id);
	}

	public ServiceTopology(String id, String type, List<SyblDirective> directives, List<Requirement> requirements,
			List<Capability> capabilities, List<ElasticityCapability> elasticityCapabilities,
			Map<String, Object> properties, List<ServiceUnit> serviceUnits, List<ServiceTopology> serviceTopologies) {
		super(id, type, directives, requirements, capabilities, elasticityCapabilities, properties);
		this.serviceUnits = serviceUnits;
		this.serviceTopologies = serviceTopologies;
	}

	public void addUnit(ServiceUnit serviceUnit) {
		if (serviceUnits == null) {
			serviceUnits = new ArrayList<ServiceUnit>();
		}
		serviceUnits.add(serviceUnit);
	}

	public void addTopology(ServiceTopology serviceTopology) {
		if (serviceTopologies == null) {
			serviceTopologies = new ArrayList<ServiceTopology>();
		}
		serviceTopologies.add(serviceTopology);
	}

	// GENERATED METHODS

	public List<ServiceUnit> getServiceUnits() {
		return serviceUnits;
	}

	public void setServiceUnits(List<ServiceUnit> serviceUnits) {
		this.serviceUnits = serviceUnits;
	}

	public List<ServiceTopology> getServiceTopologies() {
		return serviceTopologies;
	}

	public void setServiceTopologies(List<ServiceTopology> serviceTopologies) {
		this.serviceTopologies = serviceTopologies;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((serviceTopologies == null) ? 0 : serviceTopologies.hashCode());
		result = prime * result + ((serviceUnits == null) ? 0 : serviceUnits.hashCode());
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
		ServiceTopology other = (ServiceTopology) obj;
		if (serviceTopologies == null) {
			if (other.serviceTopologies != null)
				return false;
		} else if (!serviceTopologies.equals(other.serviceTopologies))
			return false;
		if (serviceUnits == null) {
			if (other.serviceUnits != null)
				return false;
		} else if (!serviceUnits.equals(other.serviceUnits))
			return false;
		return true;
	}

}
