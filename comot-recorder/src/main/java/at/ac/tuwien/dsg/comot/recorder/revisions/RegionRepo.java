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

	protected String identityNode(String id) {
		return "match (r:_REGION {_id: '" + regionId + "'})-[_MANAGE]->(n {_id: '" + id + "'}) ";
	}

	@Transactional
	public Map<String, String> extractClasses() {

		Map<String, String> properties = new HashMap<>();
		Node node = getRegion();

		for (String one : node.getPropertyKeys()) {
			if (one.equals(ManagedRegion.PROP_ID) || one.equals(ManagedRegion.PROP_TIMESTAMP)) {
				continue;
			}
			properties.put(one, node.getProperty(one).toString());
		}
		return properties;
	}

	@Transactional
	public Map<String, Object> extractPropsWithoutTime(Relationship rel) {

		Map<String, Object> properties = new HashMap<>();

		for (String one : rel.getPropertyKeys()) {
			if (one.equals(InternalRel.PROPERTY_FROM) || one.equals(InternalRel.PROPERTY_TO)) {
				continue;
			}
			properties.put(one, rel.getProperty(one));
		}
		return properties;
	}

	@Transactional
	public Map<String, Object> extractProps(Node node) {

		Map<String, Object> properties = new HashMap<>();

		for (String one : node.getPropertyKeys()) {
			properties.put(one, node.getProperty(one));
		}
		return properties;
	}

	@Transactional
	public Relationship getCurrentRelationship(Node startNode, Node endNode, String relType) {

		for (Relationship rel : startNode.getRelationships(Direction.OUTGOING,
				DynamicRelationshipType.withName(relType))) {
			if (rel.getEndNode().equals(endNode)) {
				Long to = (Long) rel.getProperty(InternalRel.PROPERTY_TO);
				if (to == Long.MAX_VALUE) {
					return rel;
				}
			}
		}
		return null;
	}

	@Transactional
	public boolean needUpdateRelationship(Relationship currentRel, InternalRel newRel) {

		if (currentRel == null) {
			return false;
		}

		return !extractPropsWithoutTime(currentRel).equals(newRel.getProperties());
	}

	@Transactional
	public boolean needUpdateState(Node currentState, InternalNode newState) {

		if (currentState == null) {
			return false;
		}
		return !extractProps(currentState).equals(newState.getProperties());
	}

	/**
	 * 
	 * @param id
	 * @param timestamp
	 *            Time when the state was valid. Use Long.MAX_VALUE for the current state.
	 * @return
	 */
	@Transactional
	public Node getState(String id, Long timestamp) {
		Node identityNode;

		if ((identityNode = getIdentityNode(id)) == null) {
			throw new IllegalArgumentException("No managed object '" + id + "' in region '" + regionId + "'");
		}

		for (Relationship rel : identityNode.getRelationships(RelTypes._HAS_STATE)) {
			Long from = (Long) rel.getProperty(InternalRel.PROPERTY_FROM);
			Long to = (Long) rel.getProperty(InternalRel.PROPERTY_TO);

			if (from < timestamp && to >= timestamp) {
				return rel.getEndNode();
			}
		}
		return null;
	}

	/**
	 * 
	 * @return null if the region does not exist
	 */
	@Transactional
	public Node getRegion() {

		// log.info("txxxx name {}", TransactionSynchronizationManager.getCurrentTransactionName());

		for (Node node : db.findNodesByLabelAndProperty(DynamicLabel.label(ManagedRegion.LABEL_REGION),
				ManagedRegion.PROP_ID,
				regionId)) {

			return node;
		}
		return null;
	}

	@Transactional
	public Iterable<Node> getAllConnectedIdentityNodes(String id, Long timestamp) {

		Iterator<Node> iter = engine.execute(
				identityNode(id) + " match p=(n)-[*]->(m:_IDENTITY) where all(x IN relationships(p) WHERE x.from < "
						+ timestamp
						+ " AND x.to >= " + timestamp + " ) unwind nodes(p) as w return distinct w").columnAs("w");

		return IteratorUtil.asIterable(iter);
	}

	@Transactional
	public Node getLastRevision() {

		Iterator<Node> iter = engine.execute(
				"match (r:_REGION {_id: '" + regionId + "'})-[rel:_LAST_REV]->(m) return m").columnAs("m");

		if (iter.hasNext()) {
			Node node = iter.next();
			if (iter.hasNext()) {
				throw new RuntimeException("getLastRevision(region=" + regionId + ") returned multiple nodes! ");
			} else {
				return node;
			}
		} else {
			throw new RuntimeException("getLastRevision(region=" + regionId + ") returned 0 nodes!");
		}
	}

	@Transactional
	public Iterable<Relationship> getAllCurrentStructuralRels() {

		Iterator<Relationship> iter = engine.execute(
				"match (r:_REGION {_id: '" + regionId
						+ "'})-[]->(n:_IDENTITY)-[rel {to: 9223372036854775807}]->(m:_IDENTITY) return rel ").columnAs(
				"rel");

		return IteratorUtil.asIterable(iter);
	}

	@Transactional
	public Iterable<Relationship> getAllStructuralRelsFromObject(String id, Long timestamp) {

		Iterator<Relationship> iter = engine.execute(
				identityNode(id) + " match (n)-[rel]->(m:_IDENTITY) where rel.from < " + timestamp + " AND rel.to >= "
						+ timestamp + " return rel ").columnAs("rel");

		return IteratorUtil.asIterable(iter);
	}

	/**
	 * 
	 * @param id
	 * @return null if there is no node with the id
	 */
	@Transactional
	public Node getIdentityNode(String id) {

		Iterator<Node> iter = engine.execute(
				identityNode(id) + " RETURN n").columnAs("n");

		if (iter.hasNext()) {
			Node node = iter.next();
			if (iter.hasNext()) {
				throw new RuntimeException("getIdentityNode( id=" + id + ", region=" + regionId
						+ ") returned multiple nodes! ");
			} else {
				log.debug("getIdentityNode(regionId={}, id={}): {}", regionId, id, node);
				return node;
			}
		} else {
			return null;
		}

	}

}
