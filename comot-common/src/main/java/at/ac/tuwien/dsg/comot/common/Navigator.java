package at.ac.tuwien.dsg.comot.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.common.model.ReferencableEntity;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServicePart;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.unit.Capability;
import at.ac.tuwien.dsg.comot.common.model.unit.Requirement;

public class Navigator {

	protected final Logger log = LoggerFactory.getLogger(Navigator.class);

	protected CloudService service;
	protected Map<String, Node> map = new HashMap<>();

	public Navigator(CloudService service) {
		this.service = service;

		map.put(service.getId(), new Node(service, null));
	}

	/**
	 * Finds the immediate parent topology of a topology, unit, capability or requirement
	 * 
	 * @param id
	 *            of topology, unit, capability or requirement
	 * @return id of immediate parent topology
	 */
	public String getParentTopologyId(String id) {

		Node node = map.get(id);
		if (node != null) {
			return node.parent.id;
		}

		for (ServiceUnit unit : getAllUnits()) {
			for (Capability capa : unit.getCapabilities()) {
				if (capa.getId().equals(id)) {
					return getParentTopologyId(unit.getId());
				}
			}
			for (Requirement req : unit.getRequirements()) {
				if (req.getId().equals(id)) {
					return getParentTopologyId(unit.getId());
				}
			}
		}

		return null;
	}

	/**
	 * Finds either unit matching with the ID or if ID is of capability or requirement returns the related service unit
	 * 
	 * @param id
	 * @return
	 */
	public ServiceUnit getUnit(String id) {

		Node node = map.get(id);
		if (node != null) {
			return (ServiceUnit) node.part;
		}

		for (ServiceUnit unit : getAllUnits()) {
			for (Capability capa : unit.getCapabilities()) {
				if (capa.getId().equals(id)) {
					return unit;
				}
			}
			for (Requirement req : unit.getRequirements()) {
				if (req.getId().equals(id)) {
					return unit;
				}
			}
		}

		return null;
	}

	public ReferencableEntity getReferencableEntity(String id) {
		Node node = map.get(id);
		if (node != null) {
			return (ReferencableEntity) node.part;
		}

		for (ServiceUnit unit : getAllUnits()) {
			for (Capability capa : unit.getCapabilities()) {
				if (capa.getId().equals(id)) {
					return capa;
				}
			}
			for (Requirement req : unit.getRequirements()) {
				if (req.getId().equals(id)) {
					return req;
				}
			}
		}
		return null;
	}

	public List<ServiceUnit> getAllUnits() {
		return getAllUnits(service.getServiceTopologies());
	}

	public static List<ServiceUnit> getAllUnits(CloudService cloudService) {
		return getAllUnits(cloudService.getServiceTopologies());
	}

	public static List<ServiceUnit> getAllUnits(List<ServiceTopology> topologies) {

		List<ServiceUnit> units = new ArrayList<>();
		List<ServiceUnit> tempUnits;

		for (ServiceTopology topology : topologies) {

			tempUnits = topology.getServiceUnits();
			units.addAll(tempUnits);

			tempUnits = getAllUnits(topology.getServiceTopologies());
			units.addAll(tempUnits);
		}

		return units;
	}

	public List<ServiceTopology> getAllTopologies() {
		return getAllTopologies(service);
	}

	public static List<ServiceTopology> getAllTopologies(CloudService cloudService) {
		List<ServiceTopology> all = getAllTopologies(cloudService.getServiceTopologies());
		all.addAll(cloudService.getServiceTopologies());
		return all;
	}

	public static List<ServiceTopology> getAllTopologies(List<ServiceTopology> topologies) {
		List<ServiceTopology> all = new ArrayList<>();
		for (ServiceTopology topology : topologies) {
			all.addAll(getAllTopologies(topology.getServiceTopologies()));
		}
		return all;
	}

	private class Node {

		private String id;
		private NodeType type;
		private ServicePart part;

		private Node parent;
		private Map<String, Node> children;

		private Node(ServicePart part, Node parent) {
			this.id = part.getId();
			this.part = part;
			this.parent = parent;

			// set type
			if (part instanceof CloudService) {
				type = NodeType.SERVICE;
				doTopologies(((CloudService) part).getServiceTopologies(), this);

			} else if (part instanceof ServiceTopology) {
				type = NodeType.TOPOLORY;
				doUnits(((ServiceTopology) part).getServiceUnits(), this);
				doTopologies(((ServiceTopology) part).getServiceTopologies(), this);

			} else if (part instanceof ServiceUnit) {
				type = NodeType.UNIT;
			}
		}

		private void doTopologies(List<ServiceTopology> topologies, Node parent) {
			for (ServiceTopology topology : topologies) {
				Node node = new Node(topology, parent);
				map.put(topology.getId(), node);
				doUnits(topology.getServiceUnits(), node);
			}
		}

		private void doUnits(List<ServiceUnit> units, Node parent) {
			log.info("{}", units);

			for (ServiceUnit unit : units) {
				map.put(unit.getId(), new Node(unit, parent));
			}
		}
	}

	enum NodeType {
		SERVICE, TOPOLORY, UNIT
	}

}
