package at.ac.tuwien.dsg.comot.common.model.structure;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.dsg.comot.common.model.SyblDirective;

public class ServiceTopology extends ServicePart {

	protected List<ServiceUnit> serviceUnits = new ArrayList<>();
	protected List<ServiceTopology> serviceTopologies = new ArrayList<>();

	public ServiceTopology() {
	}

	public ServiceTopology(String id) {
		super(id);
	}

	public ServiceTopology(
			String id,
			List<SyblDirective> directives,
			List<ServiceUnit> serviceUnits,
			List<ServiceTopology> serviceTopologies) {
		super(id, directives);
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

}
