package at.ac.tuwien.dsg.comot.m.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.model.HasUniqueId;
import at.ac.tuwien.dsg.comot.model.devel.relationship.ConnectToRel;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceEntity;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.OsuType;

public class Navigator {

	protected static final Logger log = LoggerFactory.getLogger(Navigator.class);

	protected CloudService service;
	protected Map<String, Node> map = new HashMap<>();
	protected Node root;

	public Navigator(CloudService service) {
		this.service = service;
		root = new Node(service, null);
		map.put(service.getId(), root);

		for (ServiceUnit node : getAllUnits()) {
			for (ConnectToRel rel : node.getConnectTo()) {
				map.get(rel.getTo().getId()).newNode(new HelperNode(rel.getRequirementId()));
			}
		}
	}

	public HasUniqueId getManaged(String id) {
		return map.get(id).entity;
	}

	// OSU

	public Resource getResource(String id) {

		for (ServiceUnit temp : getAllUnits()) {
			for (Resource res : temp.getOsuInstance().getOsu().getResources()) {
				if (res.getName().equals(id)) {
					return res;
				}
			}
		}
		return null;
	}

	// // RELATIONSHIP HELPERS

	public boolean isTrueServiceUnit(String id) {

		ServiceUnit node = getUnit(id);
		if (node != null) {
			if (isTrueServiceUnit(node, new HashSet<ServiceUnit>(getAllUnits()))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A node is ServiceUnit if it is NOT of type OS | DOCKER | TOMCAT and has nothing hosted on it
	 * 
	 * @param node
	 * @return
	 */
	public static boolean isTrueServiceUnit(ServiceUnit node, Set<ServiceUnit> potentialHosted) {

		boolean result = false;
		// log.info("aaaa "+OsuType.OS.toString());

		if (node.getOsuInstance().getOsu().getType().equals(OsuType.OS.toString()) ||
				node.getOsuInstance().getOsu().getType().equals(OsuType.DOCKER.toString()) ||
				node.getOsuInstance().getOsu().getType().equals(OsuType.TOMCAT.toString())) {
		} else {

			Set<ServiceUnit> hostedOnThis = getHostedOn(node, potentialHosted);

			if (hostedOnThis.isEmpty()) {
				result = true;
			}
		}

		log.debug("isServiceUnit(nodeId={} ): {}", node.getId(), result);
		return result;
	}

	public Set<UnitInstance> getHostsRecursive(UnitInstance instance) {
		Set<UnitInstance> hosts = new HashSet<>();

		if (instance.getHostInstance() != null) {
			UnitInstance host = instance.getHostInstance();
			hosts.add(host);
			hosts.addAll(getHostsRecursive(host));
		}

		return hosts;
	}

	public Set<ServiceUnit> getHostsRecursive(ServiceUnit unit) {
		Set<ServiceUnit> hosts = new HashSet<>();

		if (unit.getHost() != null) {
			ServiceUnit host = unit.getHost().getTo();
			hosts.add(host);
			hosts.addAll(getHostsRecursive(host));
		}

		return hosts;
	}

	public Set<ServiceUnit> getHostedOn(ServiceUnit node) {
		return getHostedOn(node, getParentTopologyFor(node.getId()).getServiceUnits());
	}

	public static Set<ServiceUnit> getHostedOn(ServiceUnit node, Set<ServiceUnit> potentialHosted) {

		Set<ServiceUnit> hostedOnThis = new HashSet<>();

		for (ServiceUnit one : potentialHosted) {
			if (one.getHost() != null && one.getHost().getTo().equals(node)) {
				hostedOnThis.add(one);
			}
		}
		return hostedOnThis;
	}

	public ServiceUnit getOsForServiceUnit(String id) {

		log.trace("getOsForServiceUnit(id={} )", id);

		ServiceUnit host = getHost(id);

		if (host != null) {
			if (host.getOsuInstance().getOsu().getType().equals(OsuType.OS.toString())) {
				return host;
			} else {
				return getOsForServiceUnit(host.getId());
			}
		}
		return null;
	}

	public ServiceUnit getHost(String id) {

		ServiceUnit host = ((ServiceUnit) getUnit(id));

		if (host == null || host.getHost() == null) {
			log.warn("getHost(id={}): {}", id, null);
			return null;
		} else {
			log.debug("getHost(id={}): {}", id, host.getHost().getTo());
			return host.getHost().getTo();
		}

	}

	// / NAVIGATE

	/**
	 * Finds the immediate parent topology of a topology, node, capability or requirement
	 * 
	 * @param id
	 *            of topology, unit, capability or requirement
	 * @return id of immediate parent topology
	 */
	public ServiceTopology getParentTopologyFor(String id) {

		ServiceTopology result = null;

		if (map.containsKey(id)) {
			Node node = map.get(id);

			if (node.entity.getClass().equals(ServiceTopology.class)) {
				result = (ServiceTopology) node.parent.entity;
			} else if (node.entity.getClass().equals(ServiceUnit.class)) {
				result = (ServiceTopology) node.parent.entity;
			} else if (node.entity.getClass().equals(HelperNode.class)) {
				result = (ServiceTopology) node.parent.parent.entity;
			}
		}

		log.debug("getParentTopologyFor(id={} ): {}", id, ((result == null) ? null : result.getId()));
		return result;
	}

	/**
	 * Finds either service unit matching with the ID or if ID is of capability, requirement, UnitInstance returns the
	 * related unit
	 * 
	 * @param id
	 * @return
	 */
	public ServiceUnit getUnitFor(String id) {

		ServiceUnit result = null;
		Node node = map.get(id);

		if (node != null) {
			if (node.entity.getClass().equals(ServiceUnit.class)) {
				result = (ServiceUnit) node.entity;
			} else if (node.entity.getClass().equals(HelperNode.class)) {
				result = (ServiceUnit) node.parent.entity;
			} else if (node.entity.getClass().equals(UnitInstance.class)) {
				result = (ServiceUnit) node.parent.entity;
			}
		}
		log.debug("getNodeFor(id={}): {}", id, (result == null) ? null : result.getId());
		return result;
	}

	public UnitInstance getInstance(String uniqueId) {
		Node node = map.get(uniqueId);
		if (node == null) {
			log.error("getInstance(id={}): {} (There is no instance with such id)", uniqueId, null);
		} else {
			if (node.entity instanceof UnitInstance) {
				return (UnitInstance) node.entity;
			}
		}
		return null;

	}

	public ServiceUnit getUnit(String id) {
		Node node = map.get(id);
		if (node == null) {
			log.error("getNode(id={}): {} (There is no node with such id)", id, null);
		} else {
			if (node.entity instanceof ServiceUnit) {
				return (ServiceUnit) node.entity;
			}
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

	public List<ServiceEntity> getAllServiceEntities() {
		List<ServiceEntity> list = new ArrayList<>();
		list.add(service);
		list.addAll(getAllUnits());
		list.addAll(getAllTopologies());
		return list;
	}

	public List<ServiceUnit> getAllUnits() {
		return getAllUnits(service.getServiceTopologies());
	}

	public List<ServiceTopology> getAllTopologies() {
		return getAllTopologies(service);
	}

	// STATIC

	public static List<ServiceUnit> getAllUnits(CloudService cloudService) {
		return getAllUnits(cloudService.getServiceTopologies());
	}

	public static List<ServiceUnit> getAllUnits(Collection<ServiceTopology> topologies) {

		List<ServiceUnit> units = new ArrayList<>();
		Collection<ServiceUnit> tempUnits;

		for (ServiceTopology topology : topologies) {

			tempUnits = topology.getServiceUnits();
			units.addAll(tempUnits);

			tempUnits = getAllUnits(topology.getServiceTopologies());
			units.addAll(tempUnits);
		}

		return units;
	}

	public static List<ServiceTopology> getAllTopologies(CloudService cloudService) {
		List<ServiceTopology> all = getAllTopologies(cloudService.getServiceTopologies());
		all.addAll(cloudService.getServiceTopologies());
		return all;
	}

	public static List<ServiceTopology> getAllTopologies(Collection<ServiceTopology> topologies) {
		List<ServiceTopology> all = new ArrayList<>();
		for (ServiceTopology topology : topologies) {
			all.addAll(getAllTopologies(topology.getServiceTopologies()));
		}
		return all;
	}

	// NODE

	private class Node {

		private String id;
		private HasUniqueId entity;

		private Node parent;
		private Map<String, Node> children = new HashMap<>();

		private Node(HasUniqueId entity, Node parent) {
			this.id = entity.getId();
			this.entity = entity;
			this.parent = parent;

			// set type
			if (entity instanceof CloudService) {
				for (ServiceTopology topology : ((CloudService) entity).getServiceTopologies()) {
					newNode(topology);
				}

			} else if (entity instanceof ServiceTopology) {
				for (ServiceTopology topology : ((ServiceTopology) entity).getServiceTopologies()) {
					newNode(topology);
				}
				for (ServiceUnit unit : ((ServiceTopology) entity).getServiceUnits()) {
					newNode(unit);
				}

			} else if (entity instanceof ServiceUnit) {
				ServiceUnit unit = (ServiceUnit) entity;

				for (ConnectToRel one : unit.getConnectTo()) {
					newNode(new HelperNode(one.getCapabilityId()));
				}

				for (UnitInstance instance : unit.getInstances()) {
					newNode(instance);
				}
			}
		}

		public void newNode(HasUniqueId entity) {
			Node temp = new Node(entity, this);
			children.put(temp.id, temp);
			map.put(temp.id, temp);
		}

		@Override
		public String toString() {

			return "{ \"id\" : \"" + id + "\", \"entity\" : \"" + entity + "\", \"children\" : " + children.values()
					+ "}";
		}
	}

	protected class HelperNode implements HasUniqueId {

		protected String id;

		public HelperNode(String id) {
			super();
			this.id = id;
		}

		@Override
		public String getId() {
			return id;
		}

	}

	@Override
	public String toString() {
		return "Navigator: " + root.toString();
	}

}
