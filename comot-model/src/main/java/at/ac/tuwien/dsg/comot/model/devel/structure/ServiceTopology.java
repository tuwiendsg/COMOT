package at.ac.tuwien.dsg.comot.model.devel.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServiceTopology extends ServiceEntity {

	private static final long serialVersionUID = 7319253967027446687L;

	@XmlElementWrapper(name = "ServiceUnits")
	@XmlElement(name = "Unit")
	protected Set<ServiceUnit> serviceUnits = new HashSet<>();
	@XmlElementWrapper(name = "ServiceTopologies")
	@XmlElement(name = "Topology")
	protected Set<ServiceTopology> serviceTopologies = new HashSet<>();

	public ServiceTopology() {
	}

	public ServiceTopology(String id) {
		super(id);
	}

	public ServiceTopology(
			String id,
			Set<ServiceUnit> serviceUnits,
			Set<ServiceTopology> serviceTopologies) {
		this.serviceUnits = serviceUnits;
		this.serviceTopologies = serviceTopologies;
	}

	public void addServiceUnit(ServiceUnit serviceUnit) {
		if (serviceUnits == null) {
			serviceUnits = new HashSet<>();
		}
		serviceUnits.add(serviceUnit);
	}

	public void addTopology(ServiceTopology serviceTopology) {
		if (serviceTopologies == null) {
			serviceTopologies = new HashSet<>();
		}
		serviceTopologies.add(serviceTopology);
	}

	public List<ServiceTopology> getServiceTopologiesList() {
		return new ArrayList<ServiceTopology>(serviceTopologies);
	}

	public List<ServiceUnit> getServiceUnitsList() {
		return new ArrayList<ServiceUnit>(serviceUnits);
	}

	public List<ServiceUnit> getServiceUnitList() {
		return new ArrayList<ServiceUnit>(serviceUnits);
	}

	// GENERATED METHODS

	public Set<ServiceTopology> getServiceTopologies() {
		return serviceTopologies;
	}

	public Set<ServiceUnit> getServiceUnits() {
		return serviceUnits;
	}

	public void setServiceUnits(Set<ServiceUnit> serviceUnits) {
		this.serviceUnits = serviceUnits;
	}

	public void setServiceTopologies(Set<ServiceTopology> serviceTopologies) {
		this.serviceTopologies = serviceTopologies;
	}

}
