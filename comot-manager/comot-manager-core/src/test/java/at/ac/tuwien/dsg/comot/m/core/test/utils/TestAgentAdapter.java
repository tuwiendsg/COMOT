package at.ac.tuwien.dsg.comot.m.core.test.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.events.ComotMessage;
import at.ac.tuwien.dsg.comot.m.common.events.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.events.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.events.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.events.StateMessage;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.model.type.Action;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class TestAgentAdapter {

	protected final Logger log = LoggerFactory.getLogger(getClass());

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

		channel.queueBind(queueNameLifecycle(), AppContextCore.EXCHANGE_LIFE_CYCLE, "#");
		channel.queueBind(queueNameLifecycle(), AppContextCore.EXCHANGE_CUSTOM_EVENT, "#");
		channel.queueBind(queueNameLifecycle(), AppContextCore.EXCHANGE_EXCEPTIONS, "#");

		consumerLifecycle = new QueueingConsumer(channel);
		// consumerCustom = new QueueingConsumer(channel);

		channel.basicConsume(queueNameLifecycle(), true, consumerLifecycle);
		// channel.basicConsume(queueNameCustom(), true, consumerCustom);

	}

	public void waitForLifeCycleEvent(Action action) throws JAXBException, ShutdownSignalException,
			ConsumerCancelledException, InterruptedException {

		log.info("waitForLifeCycleEvent(event={})", action);
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

		log.info("waitForCustomEvent(event={})", event);
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

		log.info("assertLifeCycleEvent(event={})", action);
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

		log.info("assertCustomEvent(event={})", event);
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
