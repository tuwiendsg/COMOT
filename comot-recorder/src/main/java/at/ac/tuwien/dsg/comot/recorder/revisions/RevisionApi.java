package at.ac.tuwien.dsg.comot.recorder.revisions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import at.ac.tuwien.dsg.comot.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.recorder.model.InternalNode;
import at.ac.tuwien.dsg.comot.recorder.model.InternalRel;
import at.ac.tuwien.dsg.comot.recorder.model.LabelTypes;
import at.ac.tuwien.dsg.comot.recorder.model.ManagedRegion;
import at.ac.tuwien.dsg.comot.recorder.model.RelTypes;
import at.ac.tuwien.dsg.comot.recorder.model.Revision;
import at.ac.tuwien.dsg.comot.recorder.repo.ChangeRepo;
import at.ac.tuwien.dsg.comot.recorder.repo.RevisionRepo;

@Component
public class RevisionApi {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApplicationContext context;

	@Autowired
	protected ChangeRepo changeRepo;
	@Autowired
	protected RevisionRepo revisionRepo;
	@Autowired
	protected GraphDatabaseService db;
	@Autowired
	protected Neo4jOperations neo;

	@Transactional
	public void createOrUpdateRegion(Object obj, String regionId, String changeType)
			throws IllegalArgumentException,
			IllegalAccessException {

		// log.info("tx {}", TransactionSynchronizationManager.isActualTransactionActive());
		// log.info("tx name {}", TransactionSynchronizationManager.getCurrentTransactionName());

		ManagedRegion region = context.getBean(ConverterToInternal.class).convertToGraph(obj);
		log.info("count nodes: {}, rels: {}", region.getNodes().size(), region.getRelationships().size());

		insertToDB(region, regionId, changeType);
	}

	protected void insertToDB(ManagedRegion region, String regionId, String changeType) {

		RegionRepo repo = context.getBean(RegionRepo.class);

		repo.setRegionId(regionId);

		Long time = System.currentTimeMillis();

		Map<InternalNode, Node> savedNodes = new HashMap<>();
		Set<Long> currentRels = new HashSet<>();
		Node identityNode, regionNode, currentState, stateNode, revisionNode, lastRevisionNode;
		Relationship stateRel, oldRel, newRel;

		Boolean update;
		Revision revision;

		// create REGION
		if ((regionNode = repo.getRegion()) == null) {
			regionNode = neo.createNode();
			regionNode.addLabel(DynamicLabel.label(ManagedRegion.LABEL_REGION));
			regionNode.setProperty(ManagedRegion.PROP_ID, regionId);
			regionNode.setProperty(ManagedRegion.PROP_TIMESTAMP, time);

			revisionNode = neo.createNode();
			revisionNode.addLabel(DynamicLabel.label("_Revision"));
			revisionNode.addLabel(DynamicLabel.label("Revision"));
			revisionNode.setProperty(Revision.PROP_ID, UUID.randomUUID().toString());

			neo.createRelationshipBetween(regionNode, revisionNode, RelTypes._FIRST_REV.toString(), null);

		} else {
			lastRevisionNode = repo.getLastRevision();
			log.info("lastRevisionNode.getId() {}", lastRevisionNode.getId());
			Revision lastRevision = revisionRepo.findOne(lastRevisionNode.getId());

			revision = revisionRepo.save(new Revision(lastRevision, changeType, time));
			revisionNode = db.getNodeById(revision.getNodeId());

			// delete old _LAST_REV
			for (Relationship rel : lastRevisionNode.getRelationships(RelTypes._LAST_REV)) {
				rel.delete();
			}
		}

		// create new _LAST_REV
		neo.createRelationshipBetween(regionNode, revisionNode, RelTypes._LAST_REV.toString(), null);

		for (String label : region.getClasses().keySet()) {
			regionNode.setProperty(label, region.getClasses().get(label));
		}

		for (InternalNode node : region.getNodes()) {

			log.info("node {}", node);

			// IDENTITY node
			if ((identityNode = repo.getIdentityNode(node.getBusinessId())) == null) {
				identityNode = neo.createNode(node.getBusinessIdAsMap(), node.getLablesForIdentityNode());

				// connect identity node with the region
				neo.createRelationshipBetween(regionNode, identityNode, RelTypes._MANAGE.toString(), null);
			}
			savedNodes.put(node, identityNode);

			// STATE
			currentState = repo.getState(node.getBusinessId(), Long.MAX_VALUE);
			update = repo.needUpdateState(currentState, node);

			if (currentState == null || update) {

				stateNode = neo.createNode(node.getProperties(), node.getLablesForStateNode());

				// connect state and identity nodes
				stateRel = neo.createRelationshipBetween(identityNode, stateNode, RelTypes._HAS_STATE.toString(), null);
				stateRel.setProperty(InternalRel.PROPERTY_FROM, time);
				stateRel.setProperty(InternalRel.PROPERTY_TO, Long.MAX_VALUE);

				if (update) {
					currentState.getRelationships(RelTypes._HAS_STATE).iterator().next()
							.setProperty(InternalRel.PROPERTY_TO, time);
				}
			}
		}

		// create structural relationships
		for (InternalRel rel : region.getRelationships()) {

			oldRel = repo.getCurrentRelationship(savedNodes.get(rel.getStartNode()), savedNodes.get(rel.getEndNode()),
					rel.getType());
			update = repo.needUpdateRelationship(oldRel, rel);

			log.info("old: {}, update: {} - {}", oldRel, update, rel);

			if (oldRel == null || update) {
				newRel = neo.createRelationshipBetween(savedNodes.get(rel.getStartNode()),
						savedNodes.get(rel.getEndNode()),
						rel.getType(), rel.getProperties());
				newRel.setProperty(InternalRel.PROPERTY_FROM, time);
				newRel.setProperty(InternalRel.PROPERTY_TO, Long.MAX_VALUE);
				currentRels.add(newRel.getId());

				if (update) {
					oldRel.setProperty(InternalRel.PROPERTY_TO, time);
				}
			} else {
				currentRels.add(oldRel.getId());
			}
		}

		// set outdated rels
		for (Relationship rel : repo.getAllCurrentStructuralRels()) {
			if (!currentRels.contains(rel.getId())) {
				rel.setProperty(InternalRel.PROPERTY_TO, time);
			}
		}

	}

	@Transactional
	public Object getRevision(String regionId, String id, Long timestamp) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException, ClassNotFoundException, RecorderException {

		ManagedRegion region = extractFromDB(regionId, id, timestamp);

		ConverterFromInternal converter = context.getBean(ConverterFromInternal.class);

		log.info("{}", region.getStartNode().getRelationships());

		Object obj = converter.convertToObject(region);
		log.info("getRevision(regionId={}, businessId={}, timestamp={}): {}", regionId, id, timestamp, obj);

		return obj;
	}

	protected ManagedRegion extractFromDB(String regionId, String id, Long timestamp) {

		RegionRepo repo = context.getBean(RegionRepo.class);
		repo.setRegionId(regionId);

		String businessId;
		Map<Long, InternalNode> nodes = new HashMap<>();
		ManagedRegion region = new ManagedRegion();
		InternalNode internalNode;

		Node identityNode = repo.getIdentityNode(id);
		if (identityNode == null) {
			throw new IllegalArgumentException("No managed object '" + id + "' in region '" + regionId + "'");
		}

		String startBusinessId = identityNode.getProperty(InternalNode.ID).toString();

		// create nodes
		for (Node connectedNode : repo.getAllConnectedIdentityNodes(id, timestamp)) {

			businessId = connectedNode.getProperty(InternalNode.ID).toString();

			internalNode = new InternalNode();
			internalNode.setProperties(repo.extractProps(repo.getState(businessId, timestamp)));
			internalNode.setBusinessId(businessId);

			// set label
			for (Label label : connectedNode.getLabels()) {
				if (label.toString().equals(LabelTypes._IDENTITY.name())) {
					continue;
				}
				internalNode.setLabel(label.name());
			}

			log.info("node: {}", internalNode);
			nodes.put(connectedNode.getId(), internalNode);

			// set start node of region
			if (startBusinessId.equals(businessId)) {
				region.setStartNode(internalNode);
			}
		}

		// create relationships
		for (InternalNode node : nodes.values()) {
			for (Relationship rel : repo.getAllStructuralRelsFromObject(node.getBusinessId(), timestamp)) {

				String type = rel.getType().name();
				InternalNode startNode = nodes.get(rel.getStartNode().getId());
				InternalNode endNode = nodes.get(rel.getEndNode().getId());

				InternalRel internalRel = new InternalRel(type, startNode, endNode, repo.extractPropsWithoutTime(rel));

				startNode.addRelationship(internalRel);
				region.addRelationship(internalRel);

				log.info("outgoing rel: {} ", internalRel);
			}
		}

		region.setNodes(new HashSet<InternalNode>(nodes.values()));
		region.setClasses(repo.extractClasses());

		return region;
	}

}
