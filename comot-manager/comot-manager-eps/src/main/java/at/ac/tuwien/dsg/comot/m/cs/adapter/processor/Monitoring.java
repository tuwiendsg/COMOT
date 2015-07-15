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
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.general.Bindings;
import at.ac.tuwien.dsg.comot.m.adapter.general.Processor;
import at.ac.tuwien.dsg.comot.m.common.InfoClient;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.eps.MonitoringClient;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.cs.adapter.ComotEvent;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
@Scope("prototype")
public class Monitoring extends Processor {

	@Autowired
	protected MonitoringClient monitoring;
	@Autowired
	protected InfoClient infoService;

	public void setHostAndPort(String host, int port) {
		monitoring.setHostAndPort(host, port);
	}

	@Override
	public Bindings getBindings(String instanceId) {

		return new Bindings()
				.addLifecycle(instanceId + ".*.*.TRUE." + State.DEPLOYING + "." + State.RUNNING + ".#")
				.addLifecycle(instanceId + "." + Action.MAINTENANCE_FINISHED + ".#")
				.addLifecycle(instanceId + "." + Action.ELASTIC_CHANGE_FINISHED + ".#")
				.addLifecycle(instanceId + ".*.*.TRUE.*." + State.PASSIVE + ".#")
				.addLifecycle(instanceId + "." + Action.TERMINATE + ".#")

				.addCustom(instanceId + ".*." + Type.SERVICE + "." + getId());
	}

	@Override
	public void onLifecycleEvent(String serviceId, String groupId, Action action, CloudService service,
			Map<String, Transition> transitions, LifeCycleEvent event) throws Exception {

		// when current RUNNING previous DEPLOYING and change TRUE
		// if monitored -> update
		// if not -> start
		if (action == Action.DEPLOYED) {

			CloudService servicefromInfo = infoService.getService(serviceId);

			if (monitoring.isMonitored(serviceId)) {
				monitoring.updateService(serviceId, servicefromInfo);
			} else {
				monitoring.startMonitoring(servicefromInfo);
			}

			// when action UPDATE_FINISHED or EL_CHANGE_FINISHED -> update
		} else if (action == Action.ELASTIC_CHANGE_FINISHED || action == Action.MAINTENANCE_FINISHED) {

			CloudService servicefromInfo = infoService.getService(serviceId);

			monitoring.updateService(serviceId, servicefromInfo);

			// when current FINAL and change TRUE -> stop
		} else if (action == Action.UNDEPLOYED) {
			if (monitoring.isMonitored(serviceId)) {
				monitoring.stopMonitoring(serviceId);
			}

		} else if (action == Action.TERMINATE && monitoring.isMonitored(serviceId)) {

			monitoring.stopMonitoring(serviceId);
		}

	}

	@Override
	public void onCustomEvent(String serviceId, String groupId, String eventName, String epsId, String optionalMessage,
			Map<String, Transition> transitions, CustomEvent event) throws Exception {

		State stateService = transitions.get(serviceId).getCurrentState();

		if (EpsEvent.EPS_SUPPORT_ASSIGNED.toString().equals(event)) {
			startIfActive(serviceId, stateService);

		} else if (EpsEvent.EPS_SUPPORT_REMOVED.toString().equals(event)) {

			if (monitoring.isMonitored(serviceId)) {
				monitoring.stopMonitoring(serviceId);
			}

		} else if (event.equals(ComotEvent.MELA_START.toString())) {
			startIfActive(serviceId, stateService);

		} else if (event.equals(ComotEvent.MELA_STOP.toString())) {
			if (monitoring.isMonitored(serviceId)) {
				monitoring.stopMonitoring(serviceId);
			}

		}
	}

	protected void startIfActive(String serviceId, State state) throws ComotException,
			ClassNotFoundException, IOException {

		if (state == State.RUNNING || state == State.ELASTIC_CHANGE || state == State.MAINTENANCE) {
			if (!monitoring.isMonitored(serviceId)) {

				CloudService servicefromInfo = infoService.getService(serviceId);

				monitoring.startMonitoring(servicefromInfo);
			}
		}
	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg) throws Exception {
		// not needed
	}

}
