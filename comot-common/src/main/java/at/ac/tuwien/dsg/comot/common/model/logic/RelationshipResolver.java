package at.ac.tuwien.dsg.comot.common.model.logic;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;
import at.ac.tuwien.dsg.comot.common.model.ReferencableEntity;
import at.ac.tuwien.dsg.comot.common.model.SyblDirective;
import at.ac.tuwien.dsg.comot.common.model.node.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.node.Capability;
import at.ac.tuwien.dsg.comot.common.model.node.Requirement;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServicePart;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.common.model.type.NodeType;
import at.ac.tuwien.dsg.comot.common.model.type.RelationshipType;

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
	public List<String> getConnectToIds(String id) {

		StackNode node = navigator.getNode(id);
		List<String> list = new ArrayList<>();

		for (EntityRelationship rel : service.getRelationships()) {
			if (rel.getType().equals(RelationshipType.CONNECT_TO)) {
				if (navigator.getNodeFor(rel.getTo().getId()).getId().equals(node.getId())) {
					list.add(navigator.getNodeFor(rel.getFrom().getId()).getId());
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

		for (EntityRelationship rel : service.getRelationships()) {
			if (rel.getType().equals(RelationshipType.HOST_ON)) {
				if (rel.getTo().getId().equals(node.getId())) {
					log.debug("isServiceUnit(nodeId={} ): false", node.getId());
					return false;
				}
			}
		}

		log.debug("isServiceUnit(nodeId={} ): true", node.getId());

		return true;
	}

	public StackNode getOsForServiceUnit(String id) {

		log.trace("getOsForServiceUnit(id={} )", id);

		StackNode host;

		for (EntityRelationship rel : service.getRelationships()) {
			if (rel.getType().equals(RelationshipType.HOST_ON)) {
				if (rel.getFrom().getId().equals(id)) {
					host = (StackNode) rel.getTo();

					log.debug("'{}' hosted_on '{}'", id, host.getId());

					if (host.getType().equals(NodeType.OS)) {
						return host;
					} else {
						return getOsForServiceUnit(host.getId());
					}
				}
			}
		}
		return null;
	}

	public StackNode getHost(String id) {

		for (EntityRelationship rel : service.getRelationships()) {
			if (rel.getType().equals(RelationshipType.HOST_ON)) {
				if (rel.getFrom().getId().equals(id)) {
					log.debug("getHost(id={}): {}", id, rel.getTo().getId());
					return (StackNode) rel.getTo();
				}
			}
		}
		log.debug("getHost(id={}): {}", id, null);
		return null;
	}

	public String getHostId(String id) {

		StackNode node = getHost(id);
		if (node != null) {
			return node.getId();
		}
		return null;
	}

	public boolean isServicePartRelationship(EntityRelationship rel) {

		ServicePart from = resolveToServicePart(rel.getFrom());
		ServicePart to = resolveToServicePart(rel.getTo());

		if (from != null && to != null) {
			return true;
		}

		return false;
	}

	public ServicePart resolveToServicePart(ReferencableEntity entity) {

		ServiceUnit unit;

		if (entity instanceof StackNode) {
			unit = navigator.getServiceUnit(entity.getId());

		} else if (entity instanceof Capability) {
			unit = navigator.getServiceUnit(navigator.getNodeFor(entity.getId()).getId());

		} else if (entity instanceof Requirement) {
			unit = navigator.getServiceUnit(navigator.getNodeFor(entity.getId()).getId());

		} else if (entity instanceof ArtifactTemplate) {
			unit = navigator.getServiceUnit(navigator.getNodeFor(entity.getId()).getId());

		} else if (entity instanceof EntityRelationship) {
			throw new UnsupportedOperationException();

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
