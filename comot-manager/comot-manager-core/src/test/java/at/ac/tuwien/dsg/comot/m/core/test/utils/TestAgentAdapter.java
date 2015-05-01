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
package at.ac.tuwien.dsg.comot.m.core.test.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.ComotMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.event.state.StateMessage;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class TestAgentAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(TestAgentAdapter.class);

	private static final String QUEUE_LIFECYCLE = "TESTER_LIFECYCLE_QUEUE_";
	private static final String QUEUE_CTUSOM = "TESTER_CUSTOM_QUEUE_";

	protected String adapterId;
	protected Connection connection;
	protected Channel channel;
	protected QueueingConsumer consumerLifecycle;
	protected QueueingConsumer consumerCustom;

	public TestAgentAdapter(String adapterId, String host) throws IOException {

		this.adapterId = adapterId;

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		connection = factory.newConnection();
		channel = connection.createChannel();

		channel.queueDeclare(queueNameLifecycle(), false, false, true, null);
		// channel.queueDeclare(queueNameCustom(), false, false, true, null);

		channel.queueBind(queueNameLifecycle(), Constants.EXCHANGE_LIFE_CYCLE, "#");
		channel.queueBind(queueNameLifecycle(), Constants.EXCHANGE_CUSTOM_EVENT, "#");
		channel.queueBind(queueNameLifecycle(), Constants.EXCHANGE_EXCEPTIONS, "#");

		consumerLifecycle = new QueueingConsumer(channel);
		// consumerCustom = new QueueingConsumer(channel);

		channel.basicConsume(queueNameLifecycle(), true, consumerLifecycle);
		// channel.basicConsume(queueNameCustom(), true, consumerCustom);

	}

	public void waitForLifeCycleEvent(Action action) throws JAXBException, ShutdownSignalException,
			ConsumerCancelledException, InterruptedException {

		LOG.info("waitForLifeCycleEvent(event={})", action);
		while (true) {
			QueueingConsumer.Delivery delivery = consumerLifecycle.nextDelivery();
			ComotMessage cMsg = Utils.asObjectFromJson(new String(delivery.getBody()), ComotMessage.class);

			if (cMsg instanceof StateMessage) {
				StateMessage msg = (StateMessage) cMsg;

				if (msg.getEvent() instanceof LifeCycleEvent) {
					LifeCycleEvent incomingEvent = (LifeCycleEvent) msg.getEvent();
					if (action == incomingEvent.getAction()) {
						return;
					}
				}

			} else if (cMsg instanceof ExceptionMessage) {
				ExceptionMessage msg = (ExceptionMessage) cMsg;
				fail("Exception " + msg);
			}
		}
	}

	public void waitForCustomEvent(String event) throws JAXBException, ShutdownSignalException,
			ConsumerCancelledException, InterruptedException {

		LOG.info("waitForCustomEvent(event={})", event);
		while (true) {
			QueueingConsumer.Delivery delivery = consumerLifecycle.nextDelivery();
			ComotMessage cMsg = Utils.asObjectFromJson(new String(delivery.getBody()), ComotMessage.class);

			if (cMsg instanceof StateMessage) {
				StateMessage msg = (StateMessage) cMsg;

				if (msg.getEvent() instanceof CustomEvent) {
					CustomEvent incomingEvent = (CustomEvent) msg.getEvent();
					if (incomingEvent.getCustomEvent().equals(event)) {
						return;
					}
				}

			} else if (cMsg instanceof ExceptionMessage) {
				ExceptionMessage msg = (ExceptionMessage) cMsg;
				fail("Exception " + msg);
			}

		}
	}

	public void assertLifeCycleEvent(Action action) throws ShutdownSignalException, ConsumerCancelledException,
			InterruptedException, JAXBException {

		LOG.info("assertLifeCycleEvent(event={})", action);
		QueueingConsumer.Delivery delivery = consumerLifecycle.nextDelivery();
		ComotMessage cMsg = Utils.asObjectFromJson(new String(delivery.getBody()), ComotMessage.class);

		if (cMsg instanceof StateMessage) {
			StateMessage msg = (StateMessage) cMsg;

			if (msg.getEvent() instanceof LifeCycleEvent) {

				LifeCycleEvent incomingEvent = (LifeCycleEvent) msg.getEvent();

				assertEquals("Unexpected Life-cycle event-state message: " + incomingEvent, action,
						incomingEvent.getAction());

			} else {
				fail("Expected Life-cycle event: '" + action + "', Actual: '" + msg.getEvent() + "'");

			}

		} else if (cMsg instanceof ExceptionMessage) {
			ExceptionMessage msg = (ExceptionMessage) cMsg;
			fail("Exception " + msg);
		}

	}

	public void assertCustomEvent(String event) throws ShutdownSignalException, ConsumerCancelledException,
			InterruptedException, JAXBException {

		LOG.info("assertCustomEvent(event={})", event);
		QueueingConsumer.Delivery delivery = consumerLifecycle.nextDelivery();
		ComotMessage cMsg = Utils.asObjectFromJson(new String(delivery.getBody()), ComotMessage.class);

		if (cMsg instanceof StateMessage) {
			StateMessage msg = (StateMessage) cMsg;

			if (msg.getEvent() instanceof CustomEvent) {

				CustomEvent incomingEvent = (CustomEvent) msg.getEvent();

				assertEquals("Unexpected Life-cycle event-state message: " + incomingEvent, event,
						incomingEvent.getCustomEvent());

			} else {
				fail("Expected Custom event: '" + event + "', Actual: '" + msg.getEvent() + "'");
			}

		} else if (cMsg instanceof ExceptionMessage) {
			ExceptionMessage msg = (ExceptionMessage) cMsg;
			fail("Exception " + msg);
		}

	}

	public String queueNameLifecycle() {
		return QUEUE_LIFECYCLE + adapterId;
	}

	public String queueNameCustom() {
		return QUEUE_CTUSOM + adapterId;
	}

	@PreDestroy
	public void clean() throws IOException {

		// if (channel != null) {
		// channel.queueDelete(queueNameLifecycle());
		// }
		// if (channel != null) {
		// channel.queueDelete(queueNameCustom());
		// }
		if (channel != null) {
			channel.close();
		}
		if (connection != null) {
			connection.close();
		}

	}
}
