/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
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
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.core.Recording;
import at.ac.tuwien.dsg.comot.m.cs.adapter.ComotEvent;
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

	private static final Logger LOG = LoggerFactory.getLogger(ElasticityAnalyzis.class);

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
	public List<ElasticPlanReport> doOneService(String serviceId) throws JAXBException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, ClassNotFoundException,
			RecorderException {

		String eventType;
		Change change;
		SyblDirective directive;
		List<ElasticPlanReport> list = new ArrayList<>();

		for (Iterator<Change> iterator = changeRepo.getAllChangesInRange(serviceId, 0L, Long.MAX_VALUE).iterator(); iterator
				.hasNext();) {
			change = iterator.next();
			eventType = Recording.extractEventName(change);

			// ACTION PLAN
			if (eventType.equals(ComotEvent.rsyblActionPlan(IEvent.Stage.START))) {

				ActionPlanEvent ap = extractEvent(change, ActionPlanEvent.class);
				ElasticPlanReport actionPlan = new ElasticPlanReport(ap, Recording.extractEventTime(change),
						change.getTimestamp());
				list.add(actionPlan);

				for (Constraint one : ap.getConstraints()) {
					directive = (SyblDirective) revisionApi.getRevision(serviceId, one.getId(), change.getTimestamp());

					one.setId(directive.getDirective());
				}

				for (Strategy one : ap.getStrategies()) {
					directive = (SyblDirective) revisionApi.getRevision(serviceId, one.getId(), change.getTimestamp());

					one.setId(directive.getDirective());
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
						actionPlan.addCustomEvent(extractEvent(change, CustomEvent.class),
								Recording.extractEventTime(change));

						// ACTION
					} else if (eventType.equals(ComotEvent.rsyblAction(IEvent.Stage.START))) {

						ActionReport action = new ActionReport(extractEvent(change, ActionEvent.class),
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
								action.addCustomEvent(extractEvent(change, CustomEvent.class), change.getTimestamp());

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

	public static <C> C extractEvent(Change change, Class<C> clazz) throws JAXBException {
		return Utils.asObjectFromJson(change.getProperty(Recording.PROP_MSG).toString(), clazz);
	}
}
