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
package at.ac.tuwien.dsg.comot.m.adapter.processor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.UtilsLc;
import at.ac.tuwien.dsg.comot.m.adapter.general.Manager;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.eps.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEventModifying;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
@Scope("prototype")
public class DeploymentHelper {

	private static final Logger LOG = LoggerFactory.getLogger(DeploymentHelper.class);

	public static long REFRESH_INTERVAL = 1000;

	protected DeploymentClient deployment;
	protected Deployment dAdapt;
	protected Manager manager;

	@Autowired
	protected DeploymentMapper mapper;
	@Autowired
	protected InformationClient infoService;

	protected String adapterId;

	@Async
	public void monitorStatusUntilDeployed(String serviceId, CloudService service)
			throws EpsException,
			ComotException, IOException, JAXBException, InterruptedException, ClassNotFoundException {

		try {

			Map<String, String> currentStates = new HashMap<>();
			Memory memory = new Memory();
			boolean notAllRunning = false;

			UtilsLc.removeProviderInfo(service);

			do {
				try {

					notAllRunning = false;
					memory.refresh(currentStates);
					currentStates = oneInteration(memory, serviceId, service);

					if (currentStates.isEmpty()) {
						notAllRunning = true;
					}

					for (String state : currentStates.values()) {
						if (DeploymentMapper.convert(state) != State.RUNNING) {
							notAllRunning = true;
						}
					}

				} catch (ComotException e) {
					LOG.warn("{}", e);
				}

			} while (notAllRunning);
			LOG.info("stopped checking");
		} catch (Exception e) {
			LOG.error("{}", e);
		}

	}

	@Async
	public Future<Object> monitoringStatusUntilInterupted(String serviceId, CloudService service) {

		LOG.info("monitoringStatusUntilInterupted(instanceId={})", serviceId);

		try {

			Map<String, String> currentStates = new HashMap<>();
			Memory memory = new Memory();

			UtilsLc.removeProviderInfo(service);

			List<UnitInstance> oldInstances = new Navigator(service).getAllUnitInstances();

			for (UnitInstance inst : oldInstances) {
				currentStates.put(inst.getId(), DeploymentMapper.runningToState());
			}

			do {
				try {

					memory.refresh(currentStates);
					currentStates = oneInteration(memory, serviceId, service);

					for (Iterator<UnitInstance> iterator = oldInstances.iterator(); iterator.hasNext();) {
						UnitInstance inst = iterator.next();

						if (!currentStates.containsKey(inst.getId())) {

							manager.sendLifeCycle(Type.INSTANCE, new LifeCycleEvent(serviceId, inst.getId(),
									Action.UNDEPLOYMENT_STARTED));
							manager.sendLifeCycle(Type.INSTANCE, new LifeCycleEvent(serviceId, inst.getId(),
									Action.UNDEPLOYED));
							iterator.remove();
						}
					}

				} catch (ComotException e) {
					LOG.warn("{}", e);
				}
			} while (true);
		} catch (InterruptedException e) {
			LOG.info("Task interrupted as expected");
		} catch (Exception e) {
			LOG.error("{}", e);
		}
		return null;

	}

	protected Map<String, String> oneInteration(Memory memory, String serviceId, CloudService service)
			throws ComotException, InterruptedException, JAXBException {

		Map<String, String> currentStates = new HashMap<>();
		CloudService serviceReturned;
		State lcStateNew;
		String stateNew;

		currentStates = new HashMap<>();

		Thread.sleep(REFRESH_INTERVAL);

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

		return currentStates;

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

			manager.sendCustom(Type.INSTANCE,
					new CustomEvent(serviceId, uInstId, stateNew, adapterId, null));

		} else {

			if (lcStateNew == State.ERROR) {
				manager.sendLifeCycle(Type.INSTANCE, new LifeCycleEvent(serviceId, uInstId, Action.ERROR));
				return;

			} else {

				if (lcStateOld == State.INIT && lcStateNew == State.DEPLOYING) {
					action = Action.DEPLOYMENT_STARTED;
				} else if (lcStateOld == State.DEPLOYING && lcStateNew == State.RUNNING) {
					action = Action.DEPLOYED;
					// } else if (lcStateOld == State.RUNNING && lcStateNew == State.UNDEPLOYING) {
					// action = Action.UNDEPLOYMENT_STARTED;
					// } else if (lcStateOld == State.UNDEPLOYING && lcStateNew == State.FINAL) {
					// action = Action.UNDEPLOYED;
				} else {
					LOG.error("invalid transitions {} -> {}", lcStateOld, lcStateNew);
					return;
				}

				Navigator nav = new Navigator(serviceReturned);

				LOG.info("creating ModifyingLifeCycleEvent for: {}, navigator: {}", uInstId, nav);

				manager.sendLifeCycle(Type.INSTANCE,
						new LifeCycleEventModifying(serviceId, uInstId, action, adapterId,
								System.currentTimeMillis(), nav.getUnitFor(uInstId).getId(), nav.getInstance(uInstId)));
			}
		}
	}

	public void setAdapterId(String adapterId) {
		this.adapterId = adapterId;
	}

	public void setDeployment(DeploymentClient deployment) {
		this.deployment = deployment;
	}

	public void setDeploymentAdapter(Deployment dAdapt) {
		this.dAdapt = dAdapt;
		this.manager = dAdapt.getManager();
	}

	public class Memory {

		Map<String, String> oldStates;
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

	}

}
