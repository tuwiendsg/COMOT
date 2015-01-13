package at.ac.tuwien.dsg.comot.model.structure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class ServiceTopology extends ServicePart {

	private static final long serialVersionUID = 7319253967027446687L;

	@XmlElementWrapper(name = "StackNodes")
	@XmlElement(name = "Node")
	protected Set<StackNode> nodes = new HashSet<>();
	@XmlElementWrapper(name = "Units")
	@XmlElement(name = "ServiceUnits")
	protected Set<ServiceUnit> serviceUnits = new HashSet<>();
	@XmlElementWrapper(name = "Topologies")
	@XmlElement(name = "ServiceTopologies")
	protected Set<ServiceTopology> serviceTopologies = new HashSet<>();

	public ServiceTopology() {
	}

	public ServiceTopology(String id) {
		super(id);
	}

	public ServiceTopology(
			String id,
			Set<StackNode> nodes,
			Set<ServiceTopology> serviceTopologies) {
		this.nodes = nodes;
		this.serviceTopologies = serviceTopologies;
	}

	public void addServiceUnit(ServiceUnit serviceUnit) {
		if (serviceUnits == null) {
			serviceUnits = new HashSet<>();
		}
		serviceUnits.add(serviceUnit);
	}

	public void addNode(StackNode node) {
		if (nodes == null) {
			nodes = new HashSet<>();
		}
		nodes.add(node);
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

	public List<StackNode> getStackNodeList() {
		return new ArrayList<StackNode>(nodes);
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

	public Set<StackNode> getNodes() {
		return nodes;
	}

	public void setNodes(Set<StackNode> nodes) {
		this.nodes = nodes;
	}

	public void setServiceTopologies(Set<ServiceTopology> serviceTopologies) {
		this.serviceTopologies = serviceTopologies;
	}

}
