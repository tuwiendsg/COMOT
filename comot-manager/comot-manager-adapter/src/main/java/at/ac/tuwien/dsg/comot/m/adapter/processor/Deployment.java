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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.xml.bind.JAXBException;

import org.springframework.amqp.core.Binding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.general.Processor;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.eps.DeploymentClient;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.StateMessage;
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

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected DeploymentClient deployment;
	@Autowired
	protected InformationClient infoService;
	@Autowired
	protected DeploymentHelper helper;

	protected Map<String, StatusTask> tasks = Collections.synchronizedMap(new HashMap<String, StatusTask>());

	@Override
	public void start() throws EpsException {

		helper.setDeploymentAdapter(this);
		helper.setAdapterId(getId());
		helper.setDeployment(deployment);

	}

	public void setHostAndPort(String host, int port) {
		deployment.setHostAndPort(host, port);
	}

	@Override
	public List<Binding> getBindings(String queueName, String instanceId) {

		List<Binding> bindings = new ArrayList<>();

		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".*.*.*." + Action.START + "." + Type.SERVICE + ".#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".*.*.*." + Action.STOP + "." + Type.SERVICE + ".#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".TRUE.*.*." + Action.DEPLOYMENT_STARTED + "." + Type.SERVICE + "." + getId() + ".#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".TRUE.*.*." + Action.UNDEPLOYMENT_STARTED + "." + Type.SERVICE + "." + getId() + ".#"));
		bindings.add(bindingCustom(queueName,
				instanceId + "." + getId() + "." + EpsEvent.EPS_SUPPORT_REMOVED + "." + Type.SERVICE + ".#"));
		bindings.add(bindingCustom(queueName,
				instanceId + "." + getId() + "." + EpsEvent.EPS_SUPPORT_ASSIGNED + "." + Type.SERVICE + ".#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".*.*.*." + Action.ELASTIC_CHANGE_STARTED + ".*.#"));
		bindings.add(bindingLifeCycle(queueName,
				instanceId + ".*.*.*." + Action.ELASTIC_CHANGE_FINISHED + ".*.#"));
		bindings.add(bindingLifeCycle(queueName,
				"*.*.*.*." + Action.KILL + ".#"));

		return bindings;
	}

	@Override
	public void onLifecycleEvent(StateMessage msg, String serviceId, String groupId,
			Action action, String optionalMessage, CloudService service, Map<String, Transition> transitions)
			throws Exception {

		if (action == Action.START && !deployment.isManaged(serviceId)) {
			manager.sendLifeCycle(Type.SERVICE,
					new LifeCycleEvent(serviceId, groupId, Action.DEPLOYMENT_STARTED));

		} else if (action == Action.STOP && deployment.isManaged(serviceId)) {
			manager.sendLifeCycle(Type.SERVICE,
					new LifeCycleEvent(serviceId, groupId, Action.UNDEPLOYMENT_STARTED));

		} else if (action == Action.DEPLOYMENT_STARTED) {
			deployInstance(serviceId);

		} else if (action == Action.UNDEPLOYMENT_STARTED) {

			unDeployInstance(serviceId);

		} else if (action == Action.ELASTIC_CHANGE_STARTED) {

			if (!tasks.containsKey(serviceId)) {
				StatusTask task = new StatusTask(serviceId, groupId, helper.monitoringStatusUntilInterupted(
						serviceId, service));
				tasks.put(serviceId, task);
			}

		} else if (action == Action.ELASTIC_CHANGE_FINISHED) {

			boolean stop = true;

			for (Transition transition : transitions.values()) {
				if (transition.getCurrentState() == State.ELASTIC_CHANGE) {
					stop = false;
				}
			}

			log.info("stop {}", stop);

			if (stop) {
				tasks.get(serviceId).getThread().cancel(true);
				tasks.remove(serviceId);
			}

		} else if (action == Action.KILL) {

			if (deployment.isManaged(serviceId)) {
				manager.sendLifeCycle(Type.SERVICE,
						new LifeCycleEvent(serviceId, groupId, Action.UNDEPLOYMENT_STARTED));
			}
		}
	}

	@Override
	public void onCustomEvent(StateMessage msg, String serviceId, String groupId, String event,
			String epsId, String originId, String optionalMessage) throws Exception {

		EpsEvent action = EpsEvent.valueOf(event);

		if (action == EpsEvent.EPS_SUPPORT_REMOVED) {

			if (deployment.isManaged(serviceId)) {

				manager.sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, serviceId,
						Action.UNDEPLOYMENT_STARTED));

				unDeployInstance(serviceId);
			}
		}
	}

	@Override
	public void onExceptionEvent(ExceptionMessage msg, String serviceId, String originId)
			throws Exception {
		// TODO Auto-generated method stub

	}

	protected void deployInstance(String serviceId) throws ClassNotFoundException, IOException,
			EpsException, ComotException, JAXBException, InterruptedException {

		// managedSet.add(instanceId);

		CloudService fullService = infoService.getService(serviceId);

		deployment.deploy(fullService);

		helper.monitorStatusUntilDeployed(serviceId, fullService);

	}

	protected void unDeployInstance(String serviceId)
			throws EpsException, ClassNotFoundException, IOException, JAXBException {

		// managedSet.remove(instanceId);

		CloudService service = infoService.getService(serviceId);

		deployment.undeploy(serviceId);

		for (ServiceUnit unit : Navigator.getAllUnits(service)) {
			unit.setInstances(new HashSet<UnitInstance>());
		}

		manager.sendLifeCycle(Type.SERVICE, new LifeCycleEvent(serviceId, serviceId, Action.UNDEPLOYED));
	}

	public class StatusTask {

		String instanceId;
		Set<String> groupIds = new HashSet<>();
		Future thread;

		public StatusTask(String instanceId, String groupId, Future thread) {
			super();
			this.instanceId = instanceId;
			this.thread = thread;
		}

		public void addGroup(String groupId) {
			groupIds.add(groupId);
		}

		public void removeGroup(String groupId) {
			groupIds.remove(groupId);
		}

		public boolean stopIfNoGroup() {
			if (groupIds.isEmpty()) {
				thread.cancel(true);
				return true;
			} else {
				return false;
			}
		}

		public Future getThread() {
			return thread;
		}

		public void setThread(Future thread) {
			this.thread = thread;
		}

	}

}
