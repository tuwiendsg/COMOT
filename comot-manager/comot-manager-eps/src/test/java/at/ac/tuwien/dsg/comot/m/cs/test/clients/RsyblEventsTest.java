package at.ac.tuwien.dsg.comot.m.cs.test.clients;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.csdg.outputProcessing.eventsNotification.ActionPlanEvent;

public class RsyblEventsTest {

	private final Logger log = LoggerFactory.getLogger(RsyblEventsTest.class);

	private String QUEUE_NAME = "events";
	private ConnectionFactory factory;
	private Connection connection;
	private Session session;
	private MessageConsumer consumer;

	@Test
	public void testProducer() throws JMSException {

		// log.info(ActiveMQConnection.DEFAULT_BROKER_URL);

		factory = new ActiveMQConnectionFactory("tcp://128.130.172.191:61616");
		// factory = new ActiveMQConnectionFactory("tcp://localhost:61616");

		connection = factory.createConnection();

		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = session.createConsumer(session.createQueue(QUEUE_NAME));

		MessageListener stopListener = new MessageListener() {

			@Override
			public void onMessage(Message message) {

				try {
					Object obj = ((ObjectMessage) message).getObject();

					log.info("XXXXXXXXX {}", obj);

					if (obj instanceof ActionPlanEvent) {
						ActionPlanEvent event = (ActionPlanEvent) obj;
						log.info("ActionPlanEvent serviceId={} type={} stage={} strategies={} constraints={}",
								event.getServiceId(), event.getType(), event.getStage(), event.getStrategies(),
								event.getConstraints());
					}

				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		};

		consumer.setMessageListener(stopListener);
		connection.start();

		log.info("sleeping");
		UtilsTest.sleepSeconds(10);

	}

	@After
	public void closeQueue() throws JMSException {

		if (consumer != null) {
			consumer.close();
		}
		if (session != null) {
			session.close();
		}
		if (connection != null) {
			connection.close();
		}

	}

}
