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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.UtilsLc;
import at.ac.tuwien.dsg.comot.m.adapter.general.IManager;
import at.ac.tuwien.dsg.comot.m.common.InfoClient;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.eps.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
@Scope("prototype")
public class DeploymentHelper {

	private static final Logger LOG = LoggerFactory.getLogger(DeploymentHelper.class);

	public static long WAIT_SHORT = 1000;
	public static long WAIT_LONG = 30000;

	protected DeploymentClient deployment;
	protected Deployment dAdapt;
	protected IManager manager;

	@Autowired
	protected DeploymentMapper mapper;
	@Autowired
	protected InfoClient infoService;

	protected String adapterId;

	@Async
	public void monitoringStatus(String serviceId, CloudService service, Deployment.Signal signal) {

		LOG.info("monitoringStatus(serviceId={})", serviceId);

		try {

			Memory memory = new Memory();
			UtilsLc.removeProviderInfo(service);

			do {
				try {

					synchronized (signal.monitor) {
						if (signal.highIntensity) {
							signal.monitor.wait(WAIT_SHORT);
						} else {
							signal.monitor.wait(WAIT_LONG);
						}
					}

					if (signal.stop) {
						break;
					}

					oneInteration(memory, serviceId, service);

				} catch (ComotException e) {
					dAdapt.getManager().sendExceptionEvent(serviceId, null, e);
					LOG.warn("{}", e);
				}

			} while (true);

			LOG.info(dAdapt.logId() + "Stopped checking state for '{}'", serviceId);

		} catch (InterruptedException e) {
			LOG.info("Task interrupted as expected");
		} catch (Exception e) {
			try {
				dAdapt.getManager().sendExceptionEvent(serviceId, null, e);
			} catch (JAXBException e1) {
				LOG.error("{}", e1);
			}
			LOG.error("{}", e);
		}
	}

	protected void oneInteration(Memory memory, String serviceId, CloudService service)
			throws ComotException, InterruptedException, JAXBException {

		Map<String, String> currentStates = new HashMap<>();
		CloudService serviceReturned;
		State lcStateNew;
		String stateNew;

		serviceReturned = deployment.refreshStatus(currentStates, service);
		serviceReturned.setId(serviceId);
		serviceReturned.setName(serviceId);

		LOG.trace("currentStates: {}", currentStates);

		for (String uInstId : currentStates.keySet()) {

			stateNew = currentStates.get(uInstId);
			lcStateNew = DeploymentMapper.convert(currentStates.get(uInstId));

			evaluateChangeOfOneUnitInstance(serviceId, uInstId, stateNew, lcStateNew,
					serviceReturned, memory);
		}

		// detect removed instances
		for (Iterator<Entry<String, State>> iterator = memory.getOldStatesLc().entrySet().iterator(); iterator
				.hasNext();) {
			Entry<String, State> entry = iterator.next();

			if (!currentStates.containsKey(entry.getKey())) {
				manager.sendLifeCycleEvent(serviceId, entry.getKey(), Action.UNDEPLOYMENT_STARTED);
				manager.sendLifeCycleEvent(serviceId, entry.getKey(), Action.UNDEPLOYED);
				iterator.remove();
			}
		}

		memory.refresh(currentStates);
	}

	protected void evaluateChangeOfOneUnitInstance(
			String serviceId, String uInstId, String stateNew, State lcStateNew,
			CloudService serviceReturned, Memory memory) throws JAXBException {

		Action action;
		State lcStateOld;

		if (memory.contains(uInstId)) {
			if (memory.oldSalas(uInstId).equals(stateNew)) {
				return;
			} else {
				lcStateOld = DeploymentMapper.convert(memory.oldSalas(uInstId));
				if (lcStateOld == null) {
					lcStateOld = memory.oldLc(uInstId);
				}
			}
		} else {
			lcStateOld = State.INIT;
		}

		LOG.info("{} old {}={}, new: {}={}", uInstId, memory.oldSalas(uInstId), lcStateOld, stateNew,
				lcStateNew);

		// check if also the translated Life-cycle state have changed
		if (lcStateNew == null || lcStateNew == lcStateOld) {

			manager.sendCustomEvent(serviceId, uInstId, stateNew, null, null);

		} else {

			if (lcStateNew == State.ERROR) {
				manager.sendLifeCycleEvent(serviceId, uInstId, Action.ERROR);
				return;

			} else {

				if (lcStateOld == State.INIT && lcStateNew == State.DEPLOYING) {
					action = Action.DEPLOYMENT_STARTED;
				} else if (lcStateOld == State.DEPLOYING && lcStateNew == State.RUNNING) {
					action = Action.DEPLOYED;
				} else {
					LOG.error("invalid transitions {} -> {}", lcStateOld, lcStateNew);
					return;
				}

				Navigator nav = new Navigator(serviceReturned);

				LOG.info("creating ModifyingLifeCycleEvent for: {}, navigator: {}", uInstId, nav);

				manager.sendLifeCycleEvent(serviceId, uInstId, action, nav.getUnitFor(uInstId).getId(),
						nav.getInstance(uInstId));
			}
		}
	}

	public void setDeployment(DeploymentClient deployment) {
		this.deployment = deployment;
	}

	public void setDeploymentAdapter(Deployment dAdapt) {
		this.dAdapt = dAdapt;
		this.manager = dAdapt.getManager();
	}

	public class Memory {

		Map<String, String> oldStates = new HashMap<>();
		Map<String, State> oldStatesLc = new HashMap<>();

		public void refresh(Map<String, String> currentStates) {

			oldStates = currentStates;
			State temp;

			for (String uInstId : currentStates.keySet()) {
				temp = DeploymentMapper.convert(oldStates.get(uInstId));
				if (temp != null) {
					oldStatesLc.put(uInstId, temp);
				}
			}
			LOG.trace("oldStates {}", oldStates);
			LOG.trace("oldStatesLc {}", oldStatesLc);
		}

		public boolean contains(String uInstId) {
			return oldStates.containsKey(uInstId);
		}

		public String oldSalas(String uInstId) {
			return oldStates.get(uInstId);
		}

		public State oldLc(String uInstId) {
			return oldStatesLc.get(uInstId);
		}

		public Map<String, State> getOldStatesLc() {
			return oldStatesLc;
		}

	}

}
