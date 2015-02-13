package at.ac.tuwien.dsg.comot.m.recorder.revisions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.neo4j.graphdb.Direction;
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

import at.ac.tuwien.dsg.comot.m.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.m.recorder.model.Change;
import at.ac.tuwien.dsg.comot.m.recorder.model.InternalNode;
import at.ac.tuwien.dsg.comot.m.recorder.model.InternalRel;
import at.ac.tuwien.dsg.comot.m.recorder.model.LabelTypes;
import at.ac.tuwien.dsg.comot.m.recorder.model.ManagedRegion;
import at.ac.tuwien.dsg.comot.m.recorder.model.RelTypes;
import at.ac.tuwien.dsg.comot.m.recorder.model.Revision;
import at.ac.tuwien.dsg.comot.m.recorder.out.ManagedObject;
import at.ac.tuwien.dsg.comot.m.recorder.repo.ChangeRepo;
import at.ac.tuwien.dsg.comot.m.recorder.repo.RevisionRepo;

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

		Node regionNode, revisionNode, lastRevisionNode;
		Relationship firstRel;
		Revision revision;
		boolean changeAlreadyCreated = false;

		Long time = System.currentTimeMillis();
		RegionRepo repo = context.getBean(RegionRepo.class);
		repo.setRegionId(regionId);

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
			neo.createRelationshipBetween(regionNode, revisionNode, RelTypes._LAST_REV.toString(), null);
			changeAlreadyCreated = true;
		}

		boolean modified = modifyEntities(region, repo, regionNode, time);

		// mark new version
		if (modified) {
			lastRevisionNode = repo.getLastRevision();
			log.info("lastRevisionNode.getId() {}", lastRevisionNode.getId());
			Revision lastRevision = revisionRepo.findOne(lastRevisionNode.getId());

			revision = revisionRepo.save(new Revision(lastRevision, changeType, time));
			revisionNode = db.getNodeById(revision.getNodeId());

			// delete old _LAST_REV
			for (Relationship rel : lastRevisionNode.getRelationships(RelTypes._LAST_REV)) {
				rel.delete();
			}
			// create new _LAST_REV
			neo.createRelationshipBetween(regionNode, revisionNode, RelTypes._LAST_REV.toString(), null);
		}

		for (String label : region.getClasses().keySet()) {
			regionNode.setProperty(label, region.getClasses().get(label));
		}

	}

	protected boolean modifyEntities(ManagedRegion region, RegionRepo repo, Node regionNode, Long time) {

		boolean modified = false;
		Map<InternalNode, Node> savedNodes = new HashMap<>();
		Set<Long> currentRels = new HashSet<>();
		Node identityNode, currentState, stateNode;
		Relationship stateRel, oldRel, newRel;

		Boolean update;

		for (InternalNode node : region.getNodes()) {

			// IDENTITY node
			if ((identityNode = repo.getIdentityNode(node.getBusinessId())) == null) {
				modified = true;

				identityNode = neo.createNode(node.getBusinessIdAsMap(), node.getLablesForIdentityNode());

				// connect identity node with the region
				neo.createRelationshipBetween(regionNode, identityNode, RelTypes._MANAGE.toString(), null);
			}
			savedNodes.put(node, identityNode);

			// STATE
			currentState = repo.getState(node.getBusinessId(), Long.MAX_VALUE);
			update = repo.needUpdateState(currentState, node);

			if (currentState == null || update) {
				modified = true;

				log.info("node modified state: {}", node);

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

			log.info("relationship old: {}, update: {} - {}", oldRel, update, rel);

			if (oldRel == null || update) {
				modified = true;
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
				modified = true;
				rel.setProperty(InternalRel.PROPERTY_TO, time);
			}
		}

		return modified;

	}

	@Transactional
	public Object getRevision(String regionId, String id, Long timestamp) throws InstantiationException,
			IllegalAccessException, IllegalArgumentException, ClassNotFoundException, RecorderException {

		ManagedRegion region = extractFromDB(regionId, id, timestamp);

		if (region.getNodes().isEmpty()) {
			return null;
		}

		ConverterFromInternal converter = context.getBean(ConverterFromInternal.class);

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

		log.info("startBusinessId {}", startBusinessId);

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

			log.info("node from DB: {}", internalNode);
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

				log.info("outgoing rel from DB: {} ", internalRel);
			}
		}

		region.setNodes(new HashSet<InternalNode>(nodes.values()));
		region.setClasses(repo.extractClasses());

		return region;
	}

	@Transactional
	public Change getAllChanges(String regionId, String id, Long from, Long to) {

		RegionRepo repo = context.getBean(RegionRepo.class);
		repo.setRegionId(regionId);

		// TODO should do this for all identity nodes that were ever connected to this node

		List<Long> list = repo.getAllChangeIdsThatInfluencedIdentityNode(id, from, to);
		List<Change> changes = new ArrayList<>();
		Change thisChange;
		Revision revision;

		for (Change one : changeRepo.getAllChangesWithTimestampsOrdered(regionId, list)) {
			changes.add(one);
		}

		// connect changes together
		for (int i = 0; i < changes.size(); i++) {
			thisChange = changes.get(i);

			if (i != 0) {
				revision = changes.get(i - 1).getTo();
				revision.setStart(changes.get(i - 1));
				revision.setEnd(thisChange);
				thisChange.setFrom(revision);

				if ((i + 1) == changes.size()) { // last
					thisChange.getTo().setStart(thisChange);
				}

			} else if (i == 0) { // first
				thisChange.getFrom().setEnd(thisChange);

				if (changes.size() == 1) { // last
					thisChange.getTo().setStart(thisChange);
				}
			}

		}

		return (changes.size() > 0) ? changes.get(0) : null;
	}

	@Transactional
	public boolean verifyObject(String regionId, String id) {

		RegionRepo repo = context.getBean(RegionRepo.class);
		repo.setRegionId(regionId);

		if (repo.getRegion() == null) {
			return false;
		}

		if (repo.getIdentityNode(id) == null) {
			return false;
		}

		return true;
	}

	@Transactional
	public List<ManagedObject> getManagedObjects(String regionId) {
		RegionRepo repo = context.getBean(RegionRepo.class);
		repo.setRegionId(regionId);

		List<ManagedObject> list = new ArrayList<>();
		ManagedObject obj;
		Node identityNode;
		for (Relationship rel : repo.getRegion().getRelationships(Direction.OUTGOING, RelTypes._MANAGE)) {
			identityNode = rel.getEndNode();
			obj = new ManagedObject();

			obj.setId(identityNode.getProperty(InternalNode.ID).toString());

			for (Label label : identityNode.getLabels()) {
				if (!label.name().equals(LabelTypes._IDENTITY.name())) {
					obj.setLabel(label.name());
					break;
				}
			}
			list.add(obj);

		}

		return list;
	}

}
