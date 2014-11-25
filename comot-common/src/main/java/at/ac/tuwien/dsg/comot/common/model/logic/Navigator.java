package at.ac.tuwien.dsg.comot.common.model.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.common.model.AbstractEntity;
import at.ac.tuwien.dsg.comot.common.model.ReferencableEntity;
import at.ac.tuwien.dsg.comot.common.model.node.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.node.Capability;
import at.ac.tuwien.dsg.comot.common.model.node.Requirement;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServicePart;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.common.model.unit.NodeInstance;

public class Navigator {

	protected final Logger log = LoggerFactory.getLogger(Navigator.class);

	protected CloudService service;
	protected Map<String, Node> map = new HashMap<>();

	public Navigator(CloudService service) {
		this.service = service;
		map.put(service.getId(), new Node(service, null));
	}

	public ServiceUnit getServiceUnit(String id) {
		for (ServiceUnit unit : getParentTopologyFor(id).getServiceUnits()) {
			if (unit.getId().equals(id)) {
				return unit;
			}
		}
		return null;
	}

	/**
	 * Finds the immediate parent topology of a topology, node, capability or requirement
	 * 
	 * @param id
	 *            of topology, unit, capability or requirement
	 * @return id of immediate parent topology
	 */
	public ServiceTopology getParentTopologyFor(String id) {

		for (Node node : map.values()) {
			if (node.entity.getClass().equals(ServiceTopology.class)) {
				return (ServiceTopology) node.parent.entity;
			} else if (node.entity.getClass().equals(StackNode.class)) {
				return (ServiceTopology) node.parent.entity;
			} else if (node.entity.getClass().equals(Capability.class) ||
					node.entity.getClass().equals(Requirement.class) ||
					node.entity.getClass().equals(ArtifactTemplate.class)) {
				return (ServiceTopology) node.parent.parent.entity;
			}
		}

		return null;
	}

	/**
	 * Finds either node matching with the ID or if ID is of capability, requirement or ArtifactTemplate returns the
	 * related node
	 * 
	 * @param id
	 * @return
	 */
	public StackNode getNodeFor(String id) {

		Node node = map.get(id);
		if (node != null) {

			if (node.entity.getClass().equals(StackNode.class)) {
				return (StackNode) node.entity;
			} else if (node.entity.getClass().equals(Capability.class) ||
					node.entity.getClass().equals(Requirement.class) ||
					node.entity.getClass().equals(ArtifactTemplate.class)) {
				return (StackNode) node.parent.entity;
			}
		}

		return null;
	}

	public NodeInstance getInstance(String id, int instanceId) {
		StackNode node = getNode(id);
		if(node == null){
			return null;
		}
		NodeInstance instance = node.getInstance(instanceId);
		
		log.debug("getInstance(id={}, instanceId={}): {}", id, instanceId, (instance == null)? null : instance.getInstanceId());
		return instance;
	}

	public StackNode getNode(String id) {
		if (map.get(id).entity instanceof StackNode) {
			return (StackNode) map.get(id).entity;
		}
		return null;
	}

	public ReferencableEntity getReferencableEntity(String id) {
		Node node = map.get(id);
		if (node != null) {
			return (ReferencableEntity) node.entity;
		}

		return null;
	}

	public ServiceTopology getTopology(String id) {
		if (map.get(id).entity instanceof ServiceTopology) {
			return (ServiceTopology) map.get(id).entity;
		}
		return null;
	}

	// GET ALL

	public List<ServicePart> getAllServiceParts() {
		List<ServicePart> list = new ArrayList<>();
		list.add(service);
		list.addAll(getAllServiceUnits());
		list.addAll(getAllTopologies());
		return list;
	}

	public List<ServiceUnit> getAllServiceUnits() {
		List<ServiceUnit> list = new ArrayList<>();
		ServiceUnit unit;

		for (StackNode node : getAllNodes()) {
			unit = getServiceUnit(node.getId());
			if (unit != null) {
				list.add(unit);
			}
		}
		return list;
	}

	public List<StackNode> getAllNodes() {
		return getAllNodes(service.getServiceTopologies());
	}

	public List<ServiceTopology> getAllTopologies() {
		return getAllTopologies(service);
	}

	// STATIC

	public static List<StackNode> getAllNodes(CloudService cloudService) {
		return getAllNodes(cloudService.getServiceTopologies());
	}

	public static List<StackNode> getAllNodes(List<ServiceTopology> topologies) {

		List<StackNode> units = new ArrayList<>();
		List<StackNode> tempUnits;

		for (ServiceTopology topology : topologies) {

			tempUnits = topology.getNodes();
			units.addAll(tempUnits);

			tempUnits = getAllNodes(topology.getServiceTopologies());
			units.addAll(tempUnits);
		}

		return units;
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

	// NODE

	private class Node {

		private String id;
		private AbstractEntity entity;

		private Node parent;
		private Map<String, Node> children;

		private Node(AbstractEntity entity, Node parent) {
			this.id = entity.getId();
			this.entity = entity;
			this.parent = parent;

			// set type
			if (entity instanceof CloudService) {
				doTopologies(((CloudService) entity).getServiceTopologies(), this);

			} else if (entity instanceof ServiceTopology) {
				doNodes(((ServiceTopology) entity).getNodes(), this);
				doTopologies(((ServiceTopology) entity).getServiceTopologies(), this);

			} else if (entity instanceof StackNode) {
				StackNode stackNode = (StackNode) entity;

				for (Capability one : stackNode.getCapabilities()) {
					map.put(one.getId(), new Node(one, this));
				}
				for (Requirement one : stackNode.getRequirements()) {
					map.put(one.getId(), new Node(one, this));
				}
				for (ArtifactTemplate one : stackNode.getDeploymentArtifacts()) {
					map.put(one.getId(), new Node(one, this));
				}

			}
		}

		private void doTopologies(List<ServiceTopology> topologies, Node parent) {
			for (ServiceTopology topology : topologies) {
				Node node = new Node(topology, parent);
				map.put(topology.getId(), node);
				doNodes(topology.getNodes(), node);
			}
		}

		private void doNodes(List<StackNode> units, Node parent) {

			for (StackNode unit : units) {
				map.put(unit.getId(), new Node(unit, parent));
			}
		}
	}

}
