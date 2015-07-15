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

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.general.Bindings;
import at.ac.tuwien.dsg.comot.m.adapter.general.IManager;
import at.ac.tuwien.dsg.comot.m.adapter.general.Processor;
import at.ac.tuwien.dsg.comot.m.common.InfoClient;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.eps.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
@Scope("prototype")
public class Deployment extends Processor {

	private static final Logger LOG = LoggerFactory.getLogger(Deployment.class);

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected DeploymentClient deployment;
	@Autowired
	protected InfoClient infoService;
	@Autowired
	protected DeploymentHelper helper;

	protected Map<String, Signal> tasks = Collections.synchronizedMap(new HashMap<String, Signal>());

	@Override
	public void init(IManager dispatcher, String participantId) {

		super.init(dispatcher, participantId);
		helper.setDeploymentAdapter(this);
		helper.setDeployment(deployment);
	}

	public void setHostAndPort(String host, int port) {
		deployment.setHostAndPort(host, port);
	}

	public IManager getManager() {
		return manager;
	}

	@Override
	public Bindings getBindings(String sId) {

		return new Bindings().addLifecycle(sId + "." + Action.START + "." + Type.SERVICE + ".#")
				.addLifecycle(sId + "." + Action.STOP + "." + Type.SERVICE + ".#")
				.addLifecycle(
						sId + "." + Action.DEPLOYMENT_STARTED + "." + Type.SERVICE + ".TRUE.*.*." + getId() + ".#")
				.addLifecycle(
						sId + "." + Action.UNDEPLOYMENT_STARTED + "." + Type.SERVICE + ".TRUE.*.*." + getId() + ".#")
				.addLifecycle(sId + "." + Action.ELASTIC_CHANGE_STARTED + ".#")
				// .addLifecycle(sId + "." + Action.ELASTIC_CHANGE_FINISHED + ".#")
				.addLifecycle(sId + ".*.*.TRUE." + State.ELASTIC_CHANGE + "." + State.RUNNING + ".#")
				.addLifecycle(sId + "." + Action.TERMINATE + ".#")
				.addLifecycle(sId + ".*.*.TRUE." + State.DEPLOYING + "." + State.RUNNING + ".#")

				.addCustom(sId + "." + EpsEvent.EPS_SUPPORT_REMOVED + "." + Type.SERVICE + "." + getId())
				.addCustom(sId + "." + EpsEvent.EPS_SUPPORT_ASSIGNED + "." + Type.SERVICE + "." + getId());
	}

	@Override
	public void onLifecycleEvent(String serviceId, String groupId, Action action, CloudService service,
			Map<String, Transition> transitions, LifeCycleEvent event) throws Exception {

		if (action == Action.START && !deployment.isManaged(serviceId)) {
			LOG.info("manager {} ", manager);

			manager.sendLifeCycleEvent(serviceId, groupId, Action.DEPLOYMENT_STARTED);

		} else if (action == Action.STOP && deployment.isManaged(serviceId)) {
			manager.sendLifeCycleEvent(serviceId, groupId, Action.UNDEPLOYMENT_STARTED);

		} else if (action == Action.DEPLOYMENT_STARTED) {
			deployService(serviceId);

		} else if (action == Action.DEPLOYED) {

			// stop intensive monitoring
			LOG.info(logId() + "stop intensive monitoring");
			tasks.get(serviceId).highIntensity = false;

		} else if (action == Action.UNDEPLOYMENT_STARTED) {

			undeployService(serviceId);

		} else if (action == Action.ELASTIC_CHANGE_STARTED) {

			LOG.info(logId() + "start intensive monitoring");
			tasks.get(serviceId).highIntensity = true;
			synchronized (tasks.get(serviceId).monitor) {
				tasks.get(serviceId).monitor.notify();
			}

		} else if (action == Action.ELASTIC_CHANGE_FINISHED) {

			// if (stop) {
			LOG.info(logId() + "stop intensive monitoring");
			tasks.get(serviceId).highIntensity = false;

		} else if (action == Action.TERMINATE && deployment.isManaged(serviceId)) {

			manager.sendLifeCycleEvent(serviceId, groupId, Action.UNDEPLOYMENT_STARTED);

		}
	}

	@Override
	public void onCustomEvent(String serviceId, String groupId, String eventName, String epsId, String optionalMessage,
			Map<String, Transition> transitions, CustomEvent event) throws Exception {

		EpsEvent action = EpsEvent.valueOf(eventName);

		if (action == EpsEvent.EPS_SUPPORT_REMOVED && deployment.isManaged(serviceId)) {

			manager.sendLifeCycleEvent(serviceId, serviceId, Action.UNDEPLOYMENT_STARTED);

			undeployService(serviceId);

		}
	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg) throws Exception {
		// not needed
	}

	protected void deployService(String serviceId) throws ClassNotFoundException, IOException,
			EpsException, ComotException, JAXBException, InterruptedException {

		CloudService fullService = infoService.getService(serviceId);

		deployment.deploy(fullService);

		LOG.info(logId() + "start monitoring");
		Signal signal = new Signal();
		tasks.put(serviceId, signal);

		helper.monitoringStatus(serviceId, fullService, signal);

	}

	protected void undeployService(String serviceId)
			throws EpsException, ClassNotFoundException, IOException, JAXBException {

		CloudService service = infoService.getService(serviceId);

		tasks.get(serviceId).stop = true;
		synchronized (tasks.get(serviceId).monitor) {
			tasks.get(serviceId).monitor.notify();
		}
		LOG.info(logId() + "stop monitoring");

		deployment.undeploy(serviceId);

		for (ServiceUnit unit : Navigator.getAllUnits(service)) {
			unit.setInstances(new HashSet<UnitInstance>());
		}

		manager.sendLifeCycleEvent(serviceId, serviceId, Action.UNDEPLOYED);
	}

	public class Signal {
		public Boolean stop = false;
		public Boolean highIntensity = true;
		public Object monitor = new Object();
	}

}
