package at.ac.tuwien.dsg.comot.graph.model.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CloudService extends ServicePart {

	private static final long serialVersionUID = 4336434946064439267L;

	protected Set<ServiceTopology> serviceTopologies = new HashSet<>();
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

	public List<ServiceTopology> getServiceTopologiesList() {
		return new ArrayList(serviceTopologies);
	}

	// GENERATED METHODS

	public Set<ServiceTopology> getServiceTopologies() {
		return serviceTopologies;
	}

	public void setServiceTopologies(Set<ServiceTopology> serviceTopologies) {
		this.serviceTopologies = serviceTopologies;
	}

	public String getAccessIp() {
		return accessIp;
	}

	public void setAccessIp(String accessIp) {
		this.accessIp = accessIp;
	}

}
