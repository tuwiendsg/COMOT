package at.ac.tuwien.dsg.comot.m.core.analytics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.ComotEvent;
import at.ac.tuwien.dsg.comot.m.core.Recording;
import at.ac.tuwien.dsg.comot.m.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.m.recorder.model.Change;
import at.ac.tuwien.dsg.comot.m.recorder.repo.ChangeRepo;
import at.ac.tuwien.dsg.comot.m.recorder.repo.RevisionRepo;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RevisionApi;
import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.csdg.elasticityInformation.elasticityRequirements.Constraint;
import at.ac.tuwien.dsg.csdg.elasticityInformation.elasticityRequirements.Strategy;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.ActionEvent;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.ActionPlanEvent;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.CustomEvent;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.IEvent;

@Component
public class ElasticityAnalyzis {

	private static final Logger log = LoggerFactory.getLogger(ElasticityAnalyzis.class);

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

	@Autowired
	protected RevisionApi revisionApi;

	@Transactional
	public void bbbb() {

		for (Change change : changeRepo.getAllChangesInRange("HelloElasticity_1", 0L, Long.MAX_VALUE)) {
			log.info("{}", change);
		}

	}

	@Transactional
	public List<ElasticPlanReport> doOneService(String serviceId, String instanceId) throws JAXBException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, ClassNotFoundException,
			RecorderException {

		String eventType;
		Change change;
		SyblDirective directive;
		List<ElasticPlanReport> list = new ArrayList<>();

		for (Iterator<Change> iterator = changeRepo.getAllChangesInRange(instanceId, 0L, Long.MAX_VALUE).iterator(); iterator
				.hasNext();) {
			change = iterator.next();
			eventType = Recording.extractEventName(change);

			log.info(eventType + " " + ComotEvent.rsyblActionPlan(IEvent.Stage.START));

			// ACTION PLAN
			if (eventType.equals(ComotEvent.rsyblActionPlan(IEvent.Stage.START))) {

				ActionPlanEvent ap = extractActionPlan(change);
				ElasticPlanReport actionPlan = new ElasticPlanReport(ap, Recording.extractEventTime(change),
						change.getTimestamp());
				list.add(actionPlan);

				for (Constraint one : ap.getConstraints()) {
					directive = (SyblDirective) revisionApi.getRevision(instanceId, one.getId(), change.getTimestamp());

					one.setId(directive.getDirective());
					// actionPlan.addDirective(directive);
				}

				for (Strategy one : ap.getStrategies()) {
					directive = (SyblDirective) revisionApi.getRevision(instanceId, one.getId(), change.getTimestamp());

					one.setId(directive.getDirective());
					// actionPlan.addDirective(directive);
				}

				while (iterator.hasNext()) {
					change = iterator.next();
					eventType = Recording.extractEventName(change);

					if (eventType.equals(ComotEvent.rsyblActionPlan(IEvent.Stage.FINISHED))) {
						actionPlan.setCurrentStage(IEvent.Stage.FINISHED);
						break;
					} else if (eventType.equals(ComotEvent.rsyblActionPlan(IEvent.Stage.FAILED))) {
						actionPlan.setCurrentStage(IEvent.Stage.FAILED);
						break;

					} else if (eventType.startsWith(ComotEvent.rsyblCustomPrefix())) {
						actionPlan.addCustomEvent(extractCustom(change), Recording.extractEventTime(change));

						// ACTION
					} else if (eventType.equals(ComotEvent.rsyblAction(IEvent.Stage.START))) {

						ActionReport action = new ActionReport(extractAction(change),
								Recording.extractEventTime(change));
						actionPlan.addActionEvent(action);

						while (iterator.hasNext()) {
							change = iterator.next();
							eventType = Recording.extractEventName(change);

							if (eventType.equals(ComotEvent.rsyblAction(IEvent.Stage.FINISHED))) {
								action.setCurrentStage(IEvent.Stage.FINISHED);
								break;
							} else if (eventType.equals(ComotEvent.rsyblAction(IEvent.Stage.FAILED))) {
								action.setCurrentStage(IEvent.Stage.FAILED);
								break;

							} else if (eventType.startsWith(ComotEvent.rsyblCustomPrefix())) {
								action.addCustomEvent(extractCustom(change), change.getTimestamp());

							} else if (eventType.equals(Action.DEPLOYED.toString())
									|| eventType.equals(Action.UNDEPLOYED.toString())) {

								action.addEffectedInstances(change.getTargetObjectId());

							}
						}
					}
				}
			}
		}

		return list;
	}

	public static ActionPlanEvent extractActionPlan(Change change) throws JAXBException {
		return Utils.asObjectFromJson(change.getProperty(Recording.PROP_MSG).toString(), ActionPlanEvent.class);
	}

	public static ActionEvent extractAction(Change change) throws JAXBException {
		return Utils.asObjectFromJson(change.getProperty(Recording.PROP_MSG).toString(), ActionEvent.class);
	}

	public static CustomEvent extractCustom(Change change) throws JAXBException {
		return Utils.asObjectFromJson(change.getProperty(Recording.PROP_MSG).toString(), CustomEvent.class);
	}
}
