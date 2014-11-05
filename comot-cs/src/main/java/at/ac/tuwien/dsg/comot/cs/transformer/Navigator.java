package at.ac.tuwien.dsg.comot.cs.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServicePart;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceUnit;

public class Navigator {

	protected CloudService service;
	protected Map<String, Node> map = new HashMap<>();

	public Navigator(CloudService service) {
		this.service = service;

		map.put(service.getId(), new Node(service, null));
	}

	public String getParentId(String unitId) {
		return map.get(unitId).parent.id;
	}

	public List<ServiceUnit> getAllUnits(CloudService cloudService) {
		return getAllUnits(cloudService.getServiceTopologies());
	}

	public List<ServiceUnit> getAllUnits(List<ServiceTopology> topologies) {

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

			}
			type = NodeType.UNIT;
		}

		private void doTopologies(List<ServiceTopology> topologies, Node parent) {

			for (ServiceTopology topology : topologies) {

				Node node = new Node(topology, parent);

				map.put(topology.getId(), node);

				doUnits(topology.getServiceUnits(), node);
			}
		}

		private void doUnits(List<ServiceUnit> units, Node parent) {
			for (ServiceUnit unit : units) {
				map.put(unit.getId(), new Node(unit, parent));

			}
		}

	}

	enum NodeType {
		SERVICE, TOPOLORY, UNIT
	}

}
