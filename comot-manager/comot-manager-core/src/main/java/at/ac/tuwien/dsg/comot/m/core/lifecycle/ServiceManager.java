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
package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.UtilsLc;
import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.event.AbstractEvent;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEventModifying;
import at.ac.tuwien.dsg.comot.m.common.event.state.ComotMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessageLifeCycle;
import at.ac.tuwien.dsg.comot.m.common.event.state.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotLifecycleException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.ComotLifecycleEvent;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;
import at.ac.tuwien.dsg.comot.model.provider.PrimitiveOperation;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
@Scope("prototype")
public class ServiceManager {

	private static final Logger LOG = LoggerFactory.getLogger(ServiceManager.class);

	public static final String LC_MANAGER_QUEUE = "LC_MANAGER_QUEUE_";

	@Autowired
	protected RabbitTemplate amqp;
	@Autowired
	protected AmqpAdmin admin;
	@Autowired
	protected ConnectionFactory connectionFactory;
	protected SimpleMessageListenerContainer container;

	@Autowired
	protected LifeCycleManager parentManager;
	@Autowired
	protected InformationClient infoService;
	@Autowired
	protected GroupManager groupManager;

	protected String serviceId;

	protected LifeCycleEvent cashedEvent;

	public String queueName() {
		return LC_MANAGER_QUEUE + serviceId;
	}

	public void createInstance(LifeCycleEvent event) throws ClassNotFoundException, IOException,
			JAXBException, EpsException {

		LOG.info("init event {}", event);

		this.serviceId = event.getServiceId();

		groupManager.setServiceId(serviceId);

		admin.declareQueue(new Queue(queueName(), false, false, true));

		container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName());
		container.setPrefetchCount(0);
		container.setMessageListener(new CustomListener());

		admin.declareBinding(new Binding(queueName(), DestinationType.QUEUE, Constants.EXCHANGE_REQUESTS,
				serviceId + ".#", null));

		try {
			executeLifeCycleEvent(event);
		} catch (ComotException e) {
			sendException(e);
		}

		container.start();

	}

	public class CustomListener implements MessageListener {

		@Override
		public void onMessage(Message message) {

			try {
				AbstractEvent event = UtilsLc.abstractEvent(message);

				LOG.info(logId() + " processing: {}", event);

				if (event instanceof LifeCycleEvent) {
					LifeCycleEvent incomingEvent = (LifeCycleEvent) event;
					if (incomingEvent.getAction() != Action.CREATED) {
						executeLifeCycleEvent(incomingEvent);
					}

				} else {
					executeCustomEvent((CustomEvent) event);
				}

			} catch (Exception e) {
				try {
					sendException(e);
					LOG.error("{}", e);
				} catch (JAXBException e1) {
					LOG.error("{}", e1);
				}
			}
		}
	}

	public void executeLifeCycleEvent(LifeCycleEvent event) throws IOException, ComotException,
			JAXBException, ClassNotFoundException {

		String groupId = event.getGroupId();
		Action action = event.getAction();
		Group targetGroup;
		CloudService service = null;
		LifeCycleEventModifying modEvent = null;

		if (event instanceof LifeCycleEventModifying) {
			modEvent = (LifeCycleEventModifying) event;
		}

		try {

			if (Action.CREATED == action) {

				if (serviceId.equals(groupId)) { // SERVICE

					service = infoService.getService(serviceId);
					groupManager.createGroupService(action, service);

				} else { // TOPOLOGY or UNIT

					if (modEvent == null) {
						throw new ComotException("The event '" + event + "' should be of type: "
								+ LifeCycleEventModifying.class);
					}

					groupManager.createGroupUnitTopo(action, modEvent.getParentId(), modEvent.getEntity());
					// TODO adapt the information model
				}

			} else if (Action.DEPLOYMENT_STARTED == action) {

				if (!groupManager.containsGroup(groupId)) { // INSTANCES

					if (modEvent == null) {
						throw new ComotException("The event '" + event + "' should be of type: "
								+ LifeCycleEventModifying.class);
					}

					groupManager.createGroupInstance(action, modEvent.getParentId(), modEvent.getInstance());
					infoService
							.putUnitInstance(serviceId, modEvent.getParentId(), modEvent.getInstance());

				} else { // ALL OTHER
					groupManager.checkAndExecute(action, groupId);
				}

			} else if (Action.DEPLOYED == action) {

				targetGroup = groupManager.checkAndExecute(action, groupId);

				if (targetGroup.getType() == Type.INSTANCE) {

					if (modEvent == null) {
						throw new ComotException("The event '" + event + "' should be of type: "
								+ LifeCycleEventModifying.class);
					}

					LOG.info("updating instance: {}", targetGroup.getId());
					infoService
							.putUnitInstance(serviceId, modEvent.getParentId(), modEvent.getInstance());

				} else {
					groupManager.checkAndExecute(action, groupId);
				}

			} else if (Action.STOP == action || Action.START_MAINTENANCE == action) {

				groupManager.checkAndExecute(action, groupId);

				boolean controllerAssigned = false;

				for (OsuInstance osuInstTemp : infoService.getService(serviceId).getSupport()) {
					for (PrimitiveOperation op : osuInstTemp.getOsu().getPrimitiveOperations()) {
						if (op instanceof ComotLifecycleEvent) {
							if (Action.valueOf(op.getExecuteMethod()) == Action.STOP_CONTROLLER) {
								controllerAssigned = true;
							}
						}
					}
				}

				if (controllerAssigned) {
					cashedEvent = event;
					event = new LifeCycleEvent(serviceId, groupId, Action.STOP_CONTROLLER, serviceId,
							System.currentTimeMillis());
				}

			} else if (Action.STOP_CONTROLLER == action) {

				event = cashedEvent;

				groupManager.checkAndExecute(event.getAction(), groupId);

			} else if (Action.UNDEPLOYED == action) {

				groupManager.checkAndExecute(action, groupId);

				for (Group temp : groupManager.getGroup(serviceId).getAllMembersNested()) {
					if (temp.getCurrentState() == State.FINAL && temp.getType() == Type.INSTANCE) {
						infoService.removeUnitInstance(serviceId, temp.getId());
					}
				}

			} else if (Action.REMOVED == action) {

				groupManager.check(action, groupId);

				if (serviceId.equals(groupId)) { // SERVICE
					service = infoService.getService(serviceId);
					infoService.removeService(serviceId);
					parentManager.removeInstanceManager(serviceId);
				}

				groupManager.checkAndExecute(action, groupId);

			} else if (Action.MOVED == action) {

				// TODO

				groupManager.checkAndExecute(action, groupId);

			} else if (Action.RECONFIGURE_ELASTICITY == action) {

				if (modEvent == null) {
					throw new ComotException("The event '" + event + "' should be of type: "
							+ LifeCycleEventModifying.class);
				}

				infoService.reconfigureElasticity(serviceId, (CloudService) modEvent.getEntity());

			} else {
				groupManager.checkAndExecute(action, groupId);
			}

			sendLifeCycleEvent(service, event);

		} catch (ComotLifecycleException e) {
			e.setEvent(event);
			throw e;
		}
	}

	public void sendLifeCycleEvent(CloudService service, LifeCycleEvent event) throws ClassNotFoundException,
			IOException, EpsException, JAXBException {

		String groupId = event.getGroupId();
		Action action = event.getAction();

		Map<String, Transition> transitions = groupManager.extractTransitions(event);

		State currentState = transitions.get(serviceId).getCurrentState();
		State previousState = transitions.get(serviceId).getLastState();

		String change = Boolean.toString(transitions.get(serviceId).isFresh()).toUpperCase();

		String bindingKey = serviceId + "." + change + "." + previousState + "." + currentState + "." + action
				+ "." + groupManager.getGroup(groupId).getType() + "." + event.getOrigin();

		// clean service
		if (service == null) {
			service = infoService.getService(serviceId);
		}
		// UtilsLc.removeProviderInfo(service);

		StateMessage message = new StateMessage(event, transitions, service);

		send(Constants.EXCHANGE_LIFE_CYCLE, bindingKey, message);

		// remove groups

		for (Iterator<Group> iterator = groupManager.getGroup(serviceId).getAllMembersNested().iterator(); iterator
				.hasNext();) {
			Group temp = iterator.next();
			if (temp.getCurrentState() == State.FINAL) {
				if (temp.getId().equals(serviceId)) {
					clean();
					
				} else {
					temp.getParent().removeMemberNested(temp.getId());
				}
			}
		}

	}

	public void executeCustomEvent(CustomEvent event) throws JAXBException, ClassNotFoundException,
			IOException, ComotException, EpsException {

		String groupId = event.getGroupId();

		if (!groupManager.containsGroup(groupId)) {
			throw new ComotException("The entity '" + groupId + "' of service instance '" + serviceId
					+ "' does not exist.");
		}

		// create binding
		String bindingKey = serviceId + "." + event.getEpsId() + "." + event.getCustomEvent() + "."
				+ groupManager.getGroup(groupId).getType();

		CloudService service = infoService.getService(serviceId);

		send(Constants.EXCHANGE_CUSTOM_EVENT, bindingKey,
				new StateMessage(event, groupManager.extractTransitions(event), service));

	}

	protected void sendException(Exception e) throws JAXBException {

		ExceptionMessage msg;

		if (e instanceof ComotLifecycleException) {
			ComotLifecycleException lcs = (ComotLifecycleException) e;

			msg = new ExceptionMessageLifeCycle(serviceId, serviceId, System.currentTimeMillis(),
					ComotLifecycleException.class.getSimpleName(), lcs.getMessage(), null, lcs.getEvent());
		} else {
			msg = new ExceptionMessage(serviceId, serviceId, System.currentTimeMillis(), e);
		}

		send(Constants.EXCHANGE_EXCEPTIONS, serviceId + "." + serviceId, msg);

	}

	protected void send(String exchange, String bindingKey, ComotMessage message) throws JAXBException {

		LOG.info(logId() + "STAT-EVENT exchange={} key={}", exchange, bindingKey);
		amqp.convertAndSend(exchange, bindingKey, Utils.asJsonString(message));
	}

	public State getCurrentState(String groupId) {
		return groupManager.getCurrentState(groupId);
	}

	public State getCurrentStateService() {
		return groupManager.getCurrentState(serviceId);
	}

	public Map<String, Transition> getCurrentState() {
		return groupManager.getCurrentState();
	}

	@PreDestroy
	public void clean() {

		if (container != null) {
			container.stop();
		}

		if (admin != null) {
			admin.deleteQueue(queueName());
		}

	}

	public String logId() {
		return "[ MANAGER_" + serviceId + "] ";
	}

}
