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
package at.ac.tuwien.dsg.comot.m.cs.adapter.processor;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.general.Bindings;
import at.ac.tuwien.dsg.comot.m.adapter.general.Processor;
import at.ac.tuwien.dsg.comot.m.common.InfoClient;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.eps.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.eps.ControlEventsListener;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.cs.adapter.ComotEvent;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.State;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.ActionEvent;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.ActionPlanEvent;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.IEvent;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

@Component
@Scope("prototype")
public class Control extends Processor implements ControlEventsListener {

	private static final Logger LOG = LoggerFactory.getLogger(Control.class);

	public static final Long TIMEOUT = 5000L;

	@Autowired
	protected ControlClient control;
	@Autowired
	protected InfoClient infoService;

	protected Set<String> managedSet = Collections.synchronizedSet(new HashSet<String>());
	protected Set<String> controlledSet = Collections.synchronizedSet(new HashSet<String>());
	protected Map<String, CompositionRulesConfiguration> mcrMap = Collections
			.synchronizedMap(new HashMap<String, CompositionRulesConfiguration>());
	protected Map<String, String> effectsMap = Collections.synchronizedMap(new HashMap<String, String>());

	public void setHostAndPort(String host, int port) {
		control.setHostAndPort(host, port);
	}

	@Override
	public Bindings getBindings(String serviceId) {

		return new Bindings()
				.addLifecycle(serviceId + ".*.*.TRUE." + State.DEPLOYING + "." + State.RUNNING + ".#")
				.addLifecycle(serviceId + "." + Action.MAINTENANCE_STARTED + ".#")
				.addLifecycle(serviceId + ".*.*.TRUE.*." + State.PASSIVE + ".#")
				.addLifecycle(serviceId + "." + Action.RECONFIGURE_ELASTICITY + ".#")
				.addLifecycle(serviceId + "." + Action.START_CONTROLLER + ".#")
				.addLifecycle(serviceId + "." + Action.STOP_CONTROLLER + ".#")
				.addLifecycle(serviceId + "." + Action.TERMINATE + ".#")

				.addCustom(serviceId + ".*." + Type.SERVICE + "." + getId());
	}

	@Override
	public void onLifecycleEvent(String serviceId, String groupId, Action action, CloudService service,
			Map<String, Transition> transitions, LifeCycleEvent event) throws Exception {

		if (action == Action.DEPLOYED) {
			control(serviceId);

		} else if (action == Action.REMOVED) {
			removeManaged(serviceId);

		} else if (action == Action.RECONFIGURE_ELASTICITY) {

			if (isManaged(serviceId)) {

				CloudService servicefromInfo = infoService.getService(serviceId);

				servicefromInfo.setId(serviceId);
				servicefromInfo.setName(serviceId);

				control.updateService(servicefromInfo);
			}

		} else if (action == Action.START_CONTROLLER) {

			control(serviceId);

		} else if (action == Action.STOP_CONTROLLER) {

			stopControl(serviceId);

			Thread.sleep(TIMEOUT);

			manager.sendLifeCycleEvent(serviceId, serviceId, action);

		} else if (action == Action.TERMINATE) {

			LOG.info("managed: {}, controlled: {}", isManaged(serviceId), isControlled(serviceId));

			removeManaged(serviceId);
		}
	}

	@Override
	public void onCustomEvent(String serviceId, String groupId, String eventName, String epsId, String optionalMessage,
			Map<String, Transition> transitions, CustomEvent event) throws Exception {

		State stateService = transitions.get(serviceId).getCurrentState();

		if (EpsEvent.EPS_SUPPORT_ASSIGNED.toString().equals(event)) {
			if (stateService == State.RUNNING) {
				control(serviceId);
			}

		} else if (EpsEvent.EPS_SUPPORT_REMOVED.toString().equals(event)) {

			removeManaged(serviceId);

		} else if (event.equals(ComotEvent.RSYBL_START.toString())) {
			if (stateService == State.RUNNING) {
				control(serviceId);
			}

		} else if (event.equals(ComotEvent.RSYBL_STOP.toString())) {

			stopControl(serviceId);

		} else if (event.equals(ComotEvent.SET_MCR.toString())) {

			CompositionRulesConfiguration mcr = Utils.asObjectFromXml(optionalMessage,
					CompositionRulesConfiguration.class);
			mcrMap.put(serviceId, mcr);

			if (isManaged(serviceId)) {
				control.updateMcr(serviceId, mcr);
			}

		} else if (event.equals(ComotEvent.RSYBL_SET_EFFECTS.toString())) {

			effectsMap.put(serviceId, optionalMessage);

			if (isManaged(serviceId)) {
				control.updateEffects(serviceId, optionalMessage);
			}
		}

	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg) throws Exception {
		// not needed
	}

	protected void manage(String serviceId) throws ClassNotFoundException, IOException,
			EpsException, JAXBException {

		if (!isManaged(serviceId)) {

			CloudService servicefromInfo = infoService.getService(serviceId);

			control.sendInitialConfig(servicefromInfo);

			managedSet.add(serviceId);
		}
	}

	protected void control(String serviceId) throws Exception {

		manage(serviceId);

		if (!isControlled(serviceId)) {

			if (mcrMap.containsKey(serviceId)) {
				control.createMcr(serviceId, mcrMap.get(serviceId));
			}

			if (effectsMap.containsKey(serviceId)) {
				control.updateEffects(serviceId, effectsMap.get(serviceId));
			}

			control.startControl(serviceId);
			control.registerForEvents(serviceId, this);
			controlledSet.add(serviceId);
		}
	}

	protected void removeManaged(String serviceId) throws EpsException {

		stopControl(serviceId);

		if (isManaged(serviceId)) {

			LOG.info("removing");

			control.removeService(serviceId);
			control.removeListener(serviceId);
			managedSet.remove(serviceId);
		}

	}

	protected void stopControl(String serviceId) throws EpsException {
		if (isControlled(serviceId)) {
			control.stopControl(serviceId);
			controlledSet.remove(serviceId);
		}
	}

	protected boolean isManaged(String instanceId) {
		return managedSet.contains(instanceId);
	}

	protected boolean isControlled(String instanceId) {
		return controlledSet.contains(instanceId);
	}

	@Override
	public void onMessage(IEvent event) {

		String serviceId = null;

		try {
			String optionalMsg = null;
			serviceId = event.getServiceId();
			String customEventName = ComotEvent.rsyblEventName(event);

			try {
				optionalMsg = Utils.asJsonString(event);
			} catch (Exception e) {
				LOG.error("Failed to marshall message: {}", event.getClass());
				LOG.error("{}", e);
				throw e;
			}

			if (event instanceof ActionPlanEvent) {
				ActionPlanEvent apEvent = (ActionPlanEvent) event;

				if (apEvent.getStage() == IEvent.Stage.START) {
					manager.sendLifeCycleEvent(serviceId, serviceId, Action.ELASTIC_CHANGE_STARTED);
					manager.sendCustomEvent(serviceId, serviceId, customEventName, null, optionalMsg);

				} else if (apEvent.getStage() == IEvent.Stage.FINISHED || apEvent.getStage() == IEvent.Stage.FINISHED) {

					manager.sendCustomEvent(serviceId, serviceId, customEventName, null, optionalMsg);
					manager.sendLifeCycleEvent(serviceId, serviceId, Action.ELASTIC_CHANGE_FINISHED);
				}

			} else {
				String targetId;
				if (event instanceof ActionEvent) {
					ActionEvent aEvent = (ActionEvent) event;

					targetId = aEvent.getTargetId();

				} else if (event instanceof at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.CustomEvent) {
					at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.CustomEvent cEvent = (at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.CustomEvent) event;
					targetId = serviceId;
				} else {
					targetId = serviceId;
				}

				manager.sendCustomEvent(serviceId, targetId, customEventName, null, optionalMsg);
			}

			LOG.info("sending custom event with optional message: {}", optionalMsg);

		} catch (Exception e) {
			try {
				manager.sendExceptionEvent(serviceId, null, e);
			} catch (Exception e1) {
				LOG.error("{}", e1);
			}
			LOG.error("{}", e);
		}
	}
}
