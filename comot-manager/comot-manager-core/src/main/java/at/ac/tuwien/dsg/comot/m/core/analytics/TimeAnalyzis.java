package at.ac.tuwien.dsg.comot.m.core.analytics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.collection.IteratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.core.Recording;
import at.ac.tuwien.dsg.comot.m.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.m.recorder.model.Change;
import at.ac.tuwien.dsg.comot.m.recorder.model.InternalNode;
import at.ac.tuwien.dsg.comot.m.recorder.model.InternalRel;
import at.ac.tuwien.dsg.comot.m.recorder.model.RelTypes;
import at.ac.tuwien.dsg.comot.m.recorder.repo.ChangeRepo;
import at.ac.tuwien.dsg.comot.m.recorder.repo.RevisionRepo;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RegionRepo;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
public class TimeAnalyzis {

	private static final Logger log = LoggerFactory.getLogger(TimeAnalyzis.class);

	@Autowired
	private ApplicationContext context;
	@Autowired
	protected GraphDatabaseService db;
	@Autowired
	protected Neo4jOperations neo;
	@Autowired
	protected ExecutionEngine engine;

	@Autowired
	protected ChangeRepo changeRepo;
	@Autowired
	protected RevisionRepo revisionRepo;

	// @Autowired
	// protected RevisionApi revisionApi;

	// @Autowired
	// protected InformationClient infoServ;

	@Transactional
	public List<ResultLine> deploymentEvents(String serviceId) throws InstantiationException,
			IllegalAccessException,
			IllegalArgumentException,
			ClassNotFoundException, RecorderException {

		List<ResultLine> resultsList = new ArrayList<>();

		RegionRepo regionRepo = context.getBean(RegionRepo.class);
		regionRepo.setRegionId(serviceId);

		for (Node unInIdentity : getAllUnitInstancesIdentityNode(serviceId)) {

			Long startTime = Long.MAX_VALUE;
			Long deplFinTime = Long.MAX_VALUE;

			for (Relationship stateRel : unInIdentity.getRelationships(RelTypes._HAS_STATE)) {

				Node unInStat = stateRel.getEndNode();
				Long fromTimeTemp = Long.parseLong(stateRel.getProperty(InternalRel.PROPERTY_FROM).toString());
				State state = State.valueOf(unInStat.getProperty("state").toString());

				if (fromTimeTemp < startTime) {
					startTime = fromTimeTemp;
				}

				if ((state == State.RUNNING || state == State.ERROR) && fromTimeTemp < deplFinTime) {
					deplFinTime = fromTimeTemp;
				}
			}

			String unitInstanceId = unInIdentity.getProperty(InternalNode.ID).toString();

			List<Change> changes = new ArrayList<>();
			for (Change one : changeRepo.getAllChangesInRangeForObject(serviceId, unitInstanceId, startTime,
					deplFinTime)) {
				// TODO remove those not of SALSA origin

				changes.add(one);
			}

			if (changes.size() < 2) {
				continue;
			}

			Node unitIdentity = unInIdentity.getSingleRelationship(DynamicRelationshipType.withName("instances"),
					Direction.INCOMING).getStartNode();
			Node osuInstanceIdentity = unitIdentity.getSingleRelationship(
					DynamicRelationshipType.withName("osuInstance"),
					Direction.OUTGOING).getEndNode();
			Node osuIdentity = osuInstanceIdentity.getSingleRelationship(DynamicRelationshipType.withName("osu"),
					Direction.OUTGOING).getEndNode();
			Node osuState = regionRepo.getStateAfterChange(osuIdentity.getProperty(InternalNode.ID).toString(),
					startTime);

			String unitId = unitIdentity.getProperty(InternalNode.ID).toString();
			String osuType = osuState.getProperty("type").toString();

			Double sum = 0.0;
			Double length;
			String name;
			ResultLine line;

			for (int i = 0; i < changes.size() - 1; i++) {

				length = (double) (((Long) changes.get(i + 1).getProperty(Recording.PROP_EVENT_TIME))
						- ((Long) changes.get(i).getProperty(Recording.PROP_EVENT_TIME))) / 1000;
				sum += length;
				name = changes.get(i).getProperty(Recording.PROP_EVENT_NAME).toString();

				line = new ResultLine(
						serviceId, unitInstanceId, unitId, osuType,
						startTime, // (Long) changes.get(i).getProperty(Recording.PROP_EVENT_TIME),
						length,
						(name.equals(Action.DEPLOYMENT_STARTED.toString()) ? "ALLOCATING" : name));

				log.info(line.toString());

				resultsList.add(line);

			}

			line = new ResultLine(
					serviceId, unitInstanceId, unitId, osuType,
					startTime, // (Long) changes.get(i).getProperty(Recording.PROP_EVENT_TIME),
					sum,
					"SUM");

			log.info(line.toString());

			resultsList.add(line);
		}

		return resultsList;
	}

	public Iterable<Node> getAllUnitInstancesIdentityNode(String instanceId) {
		Iterator<Node> iter = engine.execute(
				"match (r:_REGION {_id: '" + instanceId + "'})-[_MANAGE]->(n:" + UnitInstance.class.getSimpleName()
						+ ") return n").columnAs("n"); // get all UnitInstances
		return IteratorUtil.asIterable(iter);
	}

	protected static String identityNode(String regionId, String id) {
		return " match (r:_REGION {_id: '" + regionId + "'})-[_MANAGE]->(n {_id: '" + id + "'}) ";
	}

}
