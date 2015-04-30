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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.UtilsLc;
import at.ac.tuwien.dsg.comot.m.adapter.general.SingleQueueManager;
import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.event.AbstractEvent;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.core.EpsBuilder;
import at.ac.tuwien.dsg.comot.m.core.Recording;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
public class LifeCycleManager {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static final String MANAGER_QUEUE = "MANAGER_QUEUE";

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected AmqpAdmin admin;
	@Autowired
	protected ConnectionFactory connectionFactory;
	@Autowired
	protected RabbitTemplate amqp;

	protected SimpleMessageListenerContainer container;

	@Autowired
	protected InformationClient infoService;

	protected Map<String, ServiceManager> managers = Collections
			.synchronizedMap(new HashMap<String, ServiceManager>());

	public LifeCycleManager() {
	}

	@PostConstruct
	public void setUp() throws Exception {

		admin.declareExchange(new TopicExchange(Constants.EXCHANGE_LIFE_CYCLE, false, false));
		admin.declareExchange(new TopicExchange(Constants.EXCHANGE_CUSTOM_EVENT, false, false));
		admin.declareExchange(new TopicExchange(Constants.EXCHANGE_REQUESTS, false, false));
		admin.declareExchange(new TopicExchange(Constants.EXCHANGE_EXCEPTIONS, false, false));

		admin.declareQueue(new Queue(MANAGER_QUEUE, false, false, true));

		container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(MANAGER_QUEUE);
		container.setMessageListener(new CustomListener());

		admin.declareBinding(new Binding(MANAGER_QUEUE, DestinationType.QUEUE, Constants.EXCHANGE_REQUESTS,
				"*." + LifeCycleEvent.class.getSimpleName() + "." + Action.CREATED + "." + Type.SERVICE, null));
		admin.declareBinding(new Binding(MANAGER_QUEUE, DestinationType.QUEUE, Constants.EXCHANGE_REQUESTS,
				"*." + LifeCycleEvent.class.getSimpleName() + "." + Action.REMOVED + "." + Type.SERVICE, null));
		admin.declareBinding(new Binding(MANAGER_QUEUE, DestinationType.QUEUE, Constants.EXCHANGE_REQUESTS,
				"*." + CustomEvent.class.getSimpleName() + "." + EpsEvent.EPS_DYNAMIC_REQUESTED + ".*", null));

		container.start();

		SingleQueueManager manager1 = context.getBean(SingleQueueManager.class);
		manager1.start(Constants.EPS_BUILDER, context.getBean(EpsBuilder.class));

		SingleQueueManager manager2 = context.getBean(SingleQueueManager.class);
		manager2.start(Constants.RECORDER, context.getBean(Recording.class));

	}

	public class CustomListener implements MessageListener {

		@Override
		public void onMessage(Message message) {

			try {

				AbstractEvent abEvent = UtilsLc.abstractEvent(message);
				String serviceId = abEvent.getServiceId();

				if (abEvent instanceof LifeCycleEvent) {

					LifeCycleEvent event = (LifeCycleEvent) abEvent;

					if (Action.CREATED == event.getAction()) {
						createInstanceManager(serviceId, event);

					} else if (Action.REMOVED == event.getAction()) {
						try {
							managers.get(serviceId).groupManager.check(event.getAction(), serviceId);
							removeInstanceManager(serviceId);
						} catch (Exception e) {

						}
					}

				} else if (abEvent instanceof CustomEvent) {
					CustomEvent event = (CustomEvent) abEvent;

					if (event.getCustomEvent().equals(EpsEvent.EPS_DYNAMIC_REQUESTED.toString())) {

						String bindingKey = event.getServiceId() + "." + event.getEpsId() + "."
								+ event.getCustomEvent() + "." + Type.SERVICE;

						amqp.convertAndSend(Constants.EXCHANGE_CUSTOM_EVENT, bindingKey,
								Utils.asJsonString(new StateMessage(event, null, null)));
					}
				}

			} catch (JAXBException | ClassNotFoundException | AmqpException | IOException | EpsException e) {
				log.error("{}", e);
			}
		}
	}

	protected void createInstanceManager(String serviceId, LifeCycleEvent event) throws ClassNotFoundException,
			AmqpException, IOException, JAXBException, EpsException {

		if (managers.containsKey(serviceId)) {
			return;
		}

		ServiceManager manager = context.getBean(ServiceManager.class);
		managers.put(serviceId, manager);

		manager.createInstance(event);

	}

	protected void removeInstanceManager(String serviceId) {
		managers.remove(serviceId);

	}

	public State getCurrentState(String serviceId, String groupId) {
		return managers.get(serviceId).getCurrentState(groupId);
	}

	public State getCurrentStateService(String serviceId) {
		return managers.get(serviceId).getCurrentStateService();

	}

	public Map<String, Transition> getCurrentState(String serviceId) {
		if (managers.containsKey(serviceId)) {
			return managers.get(serviceId).getCurrentState();
		} else {
			return null;
		}
	}

	public boolean isInstanceManaged(String serviceId) {
		if (managers.containsKey(serviceId)) {
			return true;
		} else {
			return false;
		}
	}

	@PreDestroy
	public void clean() {

		if (container != null) {
			container.stop();
		}

		if (admin != null) {
			admin.deleteQueue(MANAGER_QUEUE);
		}

	}

}
