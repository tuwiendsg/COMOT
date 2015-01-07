package at.ac.tuwien.dsg.comot.recorder.revisions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import at.ac.tuwien.dsg.comot.recorder.model.InternalNode;
import at.ac.tuwien.dsg.comot.recorder.model.InternalRel;
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

		RegionRepo repo = context.getBean(RegionRepo.class);
		repo.setRegionId(regionId);

		Long time = System.currentTimeMillis();

		ManagedRegion graph = context.getBean(SingleConversion.class).convertGraph(obj);

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
			regionNode.setProperty(ManagedRegion.PROPERTY_ID, regionId);
			regionNode.setProperty("timestampId", time);

			revisionNode = neo.createNode();
			revisionNode.addLabel(DynamicLabel.label("_Revision"));
			revisionNode.addLabel(DynamicLabel.label("Revision"));
			revisionNode.setProperty("id", UUID.randomUUID().toString());

			neo.createRelationshipBetween(regionNode, revisionNode, RelTypes._FIRST_REV.toString(), null);
			neo.createRelationshipBetween(regionNode, revisionNode, RelTypes._LAST_REV.toString(), null);

		} else {
			lastRevisionNode = repo.getLastRevision();
			Revision lastRevision = revisionRepo.findOne(lastRevisionNode.getId());

			revision = revisionRepo.save(new Revision(lastRevision, changeType, time));
			revisionNode = db.getNodeById(revision.getNodeId());

			for (Relationship rel : lastRevisionNode.getRelationships(RelTypes._LAST_REV)) {
				rel.delete();
			}
		}

		for (InternalNode node : graph.getNodes()) {

			// IDENTITY node
			if ((identityNode = repo.getIdentityNode(node.getBusinessId())) == null) {
				identityNode = neo.createNode(node.getBusinessIdAsMap(), node.getLablesForIdentityNode());

				// connect identity node with the region
				neo.createRelationshipBetween(regionNode, identityNode, RelTypes._MANAGE.toString(), null);
			}
			savedNodes.put(node, identityNode);

			// STATE
			currentState = repo.getCurrentState(node.getBusinessId());
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
		for (InternalRel rel : graph.getRelationships()) {

			oldRel = repo.getRelationship(savedNodes.get(rel.getStartNode()), savedNodes.get(rel.getEndNode()),
					rel.getType());
			update = repo.needUpdateRelationship(oldRel, rel);

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
		for (Relationship rel : repo.getAllCurrentStructuralRelationships()) {
			if (!currentRels.contains(rel.getId())) {
				rel.setProperty(InternalRel.PROPERTY_TO, time);
			}
		}

	}

}
