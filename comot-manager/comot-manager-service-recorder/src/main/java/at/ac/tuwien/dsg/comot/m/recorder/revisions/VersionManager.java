package at.ac.tuwien.dsg.comot.m.recorder.revisions;

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

import at.ac.tuwien.dsg.comot.m.recorder.model.InternalNode;
import at.ac.tuwien.dsg.comot.m.recorder.model.InternalRel;
import at.ac.tuwien.dsg.comot.m.recorder.model.LabelTypes;
import at.ac.tuwien.dsg.comot.m.recorder.model.ManagedRegion;
import at.ac.tuwien.dsg.comot.m.recorder.model.RelTypes;
import at.ac.tuwien.dsg.comot.m.recorder.model.Revision;
import at.ac.tuwien.dsg.comot.m.recorder.repo.RevisionRepo;

@Component
public class VersionManager {

	protected static final Logger log = LoggerFactory.getLogger(RegionRepo.class);

	@Autowired
	private ApplicationContext context;

	@Autowired
	protected RevisionRepo revisionRepo;
	@Autowired
	protected GraphDatabaseService db;
	@Autowired
	protected Neo4jOperations neo;

	protected void insertToDB(ManagedRegion region, String regionId, String changeType,
			Map<String, String> changeProperties) {

		Node regionNode, revisionNode;

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
		}

		boolean modified = modifyEntities(region, repo, regionNode, time);

		// mark new version
		// if (modified) {
		storeChange(regionId, changeType, changeProperties, time);
		// }

		for (String label : region.getClasses().keySet()) {
			regionNode.setProperty(label, region.getClasses().get(label));
		}

	}

	protected void storeChange(String regionId, String changeType, Map<String, String> changeProperties, Long time) {

		Node regionNode, revisionNode, lastRevisionNode;
		Revision revision;

		RegionRepo repo = context.getBean(RegionRepo.class);
		repo.setRegionId(regionId);

		lastRevisionNode = repo.getLastRevision();
		regionNode = repo.getRegion();

		Revision lastRevision = revisionRepo.findOne(lastRevisionNode.getId());

		revision = revisionRepo.save(new Revision(lastRevision, changeType, changeProperties, time));
		revisionNode = db.getNodeById(revision.getNodeId());

		// delete old _LAST_REV
		for (Relationship rel : lastRevisionNode.getRelationships(RelTypes._LAST_REV)) {
			rel.delete();
		}
		// create new _LAST_REV
		neo.createRelationshipBetween(regionNode, revisionNode, RelTypes._LAST_REV.toString(), null);
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

				log.debug("node modified state: {}", node);

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

			log.debug("relationship old: {}, update: {} - {}", oldRel, update, rel);

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

			log.debug("node from DB: {}", internalNode);
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

				log.debug("outgoing rel from DB: {} ", internalRel);
			}
		}

		region.setNodes(new HashSet<InternalNode>(nodes.values()));
		region.setClasses(repo.extractClasses());

		return region;
	}

}
