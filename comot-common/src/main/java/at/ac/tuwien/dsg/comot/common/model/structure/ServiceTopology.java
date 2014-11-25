package at.ac.tuwien.dsg.comot.common.model.structure;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.dsg.comot.common.model.SyblDirective;

public class ServiceTopology extends ServicePart {

	private static final long serialVersionUID = 7319253967027446687L;

	protected List<StackNode> nodes = new ArrayList<>();
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
			List<StackNode> nodes,
			List<ServiceTopology> serviceTopologies) {
		super(id, directives);
		this.nodes = nodes;
		this.serviceTopologies = serviceTopologies;
	}

	public void addServiceUnit(ServiceUnit serviceUnit) {
		if (serviceUnits == null) {
			serviceUnits = new ArrayList<>();
		}
		serviceUnits.add(serviceUnit);
	}

	public void addNode(StackNode node) {
		if (nodes == null) {
			nodes = new ArrayList<>();
		}
		nodes.add(node);
	}

	public void addTopology(ServiceTopology serviceTopology) {
		if (serviceTopologies == null) {
			serviceTopologies = new ArrayList<>();
		}
		serviceTopologies.add(serviceTopology);
	}

	// GENERATED METHODS

	public List<ServiceTopology> getServiceTopologies() {
		return serviceTopologies;
	}

	public List<ServiceUnit> getServiceUnits() {
		return serviceUnits;
	}

	public void setServiceUnits(List<ServiceUnit> serviceUnits) {
		this.serviceUnits = serviceUnits;
	}

	public List<StackNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<StackNode> nodes) {
		this.nodes = nodes;
	}

	public void setServiceTopologies(List<ServiceTopology> serviceTopologies) {
		this.serviceTopologies = serviceTopologies;
	}

}
