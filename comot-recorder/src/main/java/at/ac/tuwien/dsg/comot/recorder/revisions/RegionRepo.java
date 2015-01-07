package at.ac.tuwien.dsg.comot.recorder.revisions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.collection.IteratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import at.ac.tuwien.dsg.comot.recorder.model.InternalNode;
import at.ac.tuwien.dsg.comot.recorder.model.InternalRel;
import at.ac.tuwien.dsg.comot.recorder.model.ManagedRegion;
import at.ac.tuwien.dsg.comot.recorder.model.RelTypes;

@Component
@Scope("prototype")
public class RegionRepo {

	protected static final Logger log = LoggerFactory.getLogger(RegionRepo.class);

	@Autowired
	protected GraphDatabaseService db;
	@Autowired
	protected Neo4jOperations neo;
	@Autowired
	protected ExecutionEngine engine;

	protected String regionId;

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	@Transactional
	public Node getLastRevision() {

		Iterator<Node> iter = engine.execute(
				"match (r:_REGION {_id: '" + regionId + "'})-[rel:_LAST_REV]->(m) return m").columnAs("m");

		for (Node node : IteratorUtil.asIterable(iter)) {
			return node;
		}
		return null;
	}

	@Transactional
	public Iterable<Relationship> getAllCurrentStructuralRelationships() {

		Iterator<Relationship> iter = engine.execute(
				"match (r:_REGION {_id: '" + regionId + "'})-[]->(n:_IDENTITY)-[rel {to: " + Long.MAX_VALUE
						+ "}]->(m:_IDENTITY) return rel ").columnAs("rel");

		return IteratorUtil.asIterable(iter);
	}

	@Transactional
	public Relationship getRelationship(Node startNode, Node endNode, String relType) {

		for (Relationship rel : startNode.getRelationships(Direction.OUTGOING,
				DynamicRelationshipType.withName(relType))) {
			if (rel.getEndNode().equals(endNode)) {
				return rel;
			}
		}

		return null;
	}

	@Transactional
	public boolean needUpdateRelationship(Relationship currentRel, InternalRel newRel) {

		if (currentRel == null) {
			return false;
		}

		Map<String, Object> properties = new HashMap<>();

		// check properties
		for (String one : currentRel.getPropertyKeys()) {
			if (one.equals(InternalRel.PROPERTY_FROM) || one.equals(InternalRel.PROPERTY_TO)) {
				continue;
			} else {
				properties.put(one, currentRel.getProperty(one));
			}
		}
		return !properties.equals(newRel.getProperties());
	}

	@Transactional
	public boolean needUpdateState(Node currentState, InternalNode newState) {

		if (currentState == null) {
			return false;
		}

		Map<String, Object> properties = new HashMap<>();

		for (String one : currentState.getPropertyKeys()) {
			properties.put(one, currentState.getProperty(one));
		}

		return !properties.equals(newState.getProperties());
	}

	@Transactional
	public Node getCurrentState(String id) {
		Node identityNode;

		if ((identityNode = getIdentityNode(id)) == null) {
			return null;
		}

		for (Relationship rel : identityNode.getRelationships(RelTypes._HAS_STATE)) {
			if (rel.getProperty(InternalRel.PROPERTY_TO).equals(Long.MAX_VALUE)) {
				return rel.getEndNode();
			}
		}
		return null;
	}

	@Transactional
	public Node getRegion() {

		// log.info("txxxx name {}", TransactionSynchronizationManager.getCurrentTransactionName());

		for (Node node : db.findNodesByLabelAndProperty(DynamicLabel.label(ManagedRegion.LABEL_REGION),
				ManagedRegion.PROPERTY_ID,
				regionId)) {

			return node;
		}
		return null;
	}

	@Transactional
	public Node getIdentityNode(String id) {

		Iterator<Node> iter = engine.execute(
				"MATCH r, n WHERE (r:_REGION {_id : '" + regionId + "' })-[:_MANAGE]->(n:_IDENTITY {_id : '" + id
						+ "' }) RETURN n").columnAs("n");

		for (Node node : IteratorUtil.asIterable(iter)) {

			log.info("getIdentityNode(regionId={}, id={}): {}", regionId, id, node);
			return node;
		}
		return null;
	}

}
