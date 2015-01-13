package at.ac.tuwien.dsg.comot.common.model.logic;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.common.model.logic.Navigator.HelperNode;
import at.ac.tuwien.dsg.comot.model.HasUniqueId;
import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.comot.model.relationship.ConnectToRel;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.structure.ServicePart;
import at.ac.tuwien.dsg.comot.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.model.type.NodeType;

public class RelationshipResolver {

	protected final Logger log = LoggerFactory.getLogger(RelationshipResolver.class);

	protected CloudService service;
	protected Navigator navigator;

	public RelationshipResolver(CloudService service) {
		this.service = service;
		navigator = new Navigator(service);
	}

	/**
	 * In inverse direction then in tosca.
	 * 
	 * @param id
	 * @return
	 */
	public Set<String> getConnectToIds(String id) {

		Set<String> list = new HashSet<>();

		for (StackNode one : navigator.getAllNodes()) {
			for (ConnectToRel rel : one.getConnectTo()) {
				if (rel.getTo().getId().equals(id)) {
					list.add(rel.getFrom().getId());
				}
			}
		}

		return list;
	}

	public boolean isServiceUnit(String id) {
		if (navigator.getNode(id) != null) {
			if (isServiceUnit(navigator.getNode(id))) {
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
	public boolean isServiceUnit(StackNode node) {

		if (node.getType().equals(NodeType.OS) ||
				node.getType().equals(NodeType.DOCKER) ||
				node.getType().equals(NodeType.TOMCAT)) {
			log.debug("isServiceUnit(nodeId={} ): false", node.getId());
			return false;
		}

		for (StackNode one : navigator.getAllNodes()) {
			if (one.getHostNode() != null && one.getHostNode().getId().equals(node.getId())) {
				log.debug("isServiceUnit(nodeId={} ): false", node.getId());
				return false;
			}
		}

		log.debug("isServiceUnit(nodeId={} ): true", node.getId());

		return true;
	}

	public StackNode getOsForServiceUnit(String id) {

		log.trace("getOsForServiceUnit(id={} )", id);

		StackNode host = getHost(id);

		if (host != null) {
			if (host.getType().equals(NodeType.OS)) {
				return host;
			} else {
				return getOsForServiceUnit(host.getId());
			}
		}
		return null;
	}

	public String getHostId(String id) {

		StackNode node = getHost(id);
		if (node != null) {
			return node.getId();
		}
		return null;
	}

	public StackNode getHost(String id) {

		StackNode host = ((StackNode) navigator.getNode(id));

		if (host == null || host.getHostNode() == null) {
			log.warn("getHost(id={}): {}", id, null);
			return null;
		} else {
			log.debug("getHost(id={}): {}", id, host.getHostNode().getTo());
			return host.getHostNode().getTo();
		}

	}

	// public boolean isServicePartRelationship(EntityRelationship rel) {
	//
	// ServicePart from = resolveToServicePart(rel.getFrom());
	// ServicePart to = resolveToServicePart(rel.getTo());
	//
	// if (from != null && to != null) {
	// return true;
	// }
	//
	// return false;
	// }

	public ServicePart resolveToServicePart(String id) {

		ServiceUnit unit;

		HasUniqueId entity = navigator.getAbstractEntity(id);

		if (entity instanceof StackNode) {
			unit = navigator.getServiceUnit(entity.getId());

		} else if (entity instanceof HelperNode) {
			unit = navigator.getServiceUnit(navigator.getNodeFor(entity.getId()).getId());

		} else if (entity instanceof SyblDirective) {
			throw new UnsupportedOperationException();

		} else {
			throw new UnsupportedOperationException();
		}
		log.debug("resolveToServicePart(entityId={} ): servicePartId={}", entity.getId(),
				(unit == null) ? null : unit.getId());
		return unit;
	}

	public Navigator navigator() {
		return navigator;
	}

}
