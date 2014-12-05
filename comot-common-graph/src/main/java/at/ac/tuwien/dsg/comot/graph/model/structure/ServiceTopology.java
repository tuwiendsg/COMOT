package at.ac.tuwien.dsg.comot.graph.model.structure;

import java.util.HashSet;
import java.util.Set;

import at.ac.tuwien.dsg.comot.graph.model.SyblDirective;

public class ServiceTopology extends ServicePart {

	private static final long serialVersionUID = 7319253967027446687L;

	protected Set<StackNode> nodes = new HashSet<>();
	protected Set<ServiceUnit> serviceUnits = new HashSet<>();
	protected Set<ServiceTopology> serviceTopologies = new HashSet<>();

	public ServiceTopology() {
	}

	public ServiceTopology(String id) {
		super(id);
	}

	public ServiceTopology(
			String id,
			Set<SyblDirective> directives,
			Set<StackNode> nodes,
			Set<ServiceTopology> serviceTopologies) {
		super(id, directives);
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
