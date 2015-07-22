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
package at.ac.tuwien.dsg.comot.m.adapter.general;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.MngPath;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEventModifying;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceEntity;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;

public abstract class Manager implements IManager {

	private static final Logger LOG = LoggerFactory.getLogger(Manager.class);

	public static final String ADAPTER_QUEUE = "ADAPTER_QUEUE_";

	@Autowired
	protected ApplicationContext context;

	@Autowired
	protected AmqpAdmin admin;
	@Autowired
	protected ConnectionFactory connectionFactory;
	@Autowired
	protected RabbitTemplate amqp;

	protected String participantId;
	protected IProcessor processor;

	public void startStandalone(String baseUri, OfferedServiceUnit eps, IProcessor processor) throws Exception {

		Client client = ClientBuilder.newClient();

		Response response = client.target(baseUri)
				.path(MngPath.EPS_EXTERNAL)
				.request(MediaType.WILDCARD)
				.post(Entity.xml(eps));
		String epsInstanceId = response.readEntity(String.class);
		client.close();

		start(epsInstanceId, processor);
	}

	public void start(String participantId, IProcessor processor) throws Exception {
		this.participantId = participantId;
		this.processor = processor;

		processor.init(this, participantId);
		start();

		LOG.info("started participant: '{}'", participantId);
	}

	public abstract void start();

	@Override
	public abstract void stop();

	@Override
	public void sendLifeCycleEvent(String serviceId, String groupId, Action action) throws JAXBException {
		sendLifeCycle(new LifeCycleEvent(serviceId, groupId, action));
	}

	@Override
	public void sendLifeCycleEvent(String serviceId, String groupId, Action action, String parentId,
			ServiceEntity entity)
			throws JAXBException {
		sendLifeCycle(new LifeCycleEventModifying(serviceId, groupId, action, parentId, entity));
	}

	@Override
	public void sendLifeCycleEvent(String serviceId, String groupId, Action action, String parentId,
			UnitInstance instance)
			throws JAXBException {
		sendLifeCycle(new LifeCycleEventModifying(serviceId, groupId, action, parentId, instance));
	}

	@Override
	public void sendLifeCycle(LifeCycleEvent event) throws JAXBException {

		event.setOrigin(getId());
		event.setTime(System.currentTimeMillis());

		String bindingKey = event.getServiceId() + "." + LifeCycleEvent.class.getSimpleName() + "."
				+ event.getAction()
				+ "." + event.getGroupId();

		LOG.info(logId() + "EVENT-LC key={}", bindingKey);

		amqp.convertAndSend(Constants.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));
	}

	@Override
	public void sendCustomEvent(String serviceId, String groupId, String eventName, String epsId, String message,
			String correlationId) throws JAXBException {

		CustomEvent event = new CustomEvent(serviceId, groupId, eventName, epsId, message);
		event.setCorrelationId(correlationId);

		LOG.info("correlationId {}", correlationId);

		sendCustomEvent(event);
	}

	@Override
	public void sendCustomEvent(String serviceId, String groupId, String eventName, String epsId, String message)
			throws JAXBException {

		CustomEvent event = new CustomEvent(serviceId, groupId, eventName, epsId, message);

		sendCustomEvent(event);
	}

	@Override
	public void sendCustomEvent(CustomEvent event) throws JAXBException {

		event.setOrigin(getId());
		event.setTime(System.currentTimeMillis());

		String bindingKey = event.getServiceId() + "." + event.getClass().getSimpleName() + "."
				+ event.getCustomEvent() + "." + event.getGroupId();

		LOG.info(logId() + "EVENT-CUST key={}", bindingKey);

		amqp.convertAndSend(Constants.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));
	}

	@Override
	public void sendExceptionEvent(String serviceId, String eventCauseId, Exception e) throws JAXBException {

		ExceptionMessage msg = new ExceptionMessage(serviceId, getId(), System.currentTimeMillis(), eventCauseId, e);

		String bindingKey = serviceId + "." + getId();

		amqp.convertAndSend(Constants.EXCHANGE_EXCEPTIONS, bindingKey, Utils.asJsonString(msg));

	}

	@Override
	public String logId() {
		return "[" + participantId + "] ";
	}

	protected String getId() {
		return participantId;
	}
}
