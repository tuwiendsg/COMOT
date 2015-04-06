package at.ac.tuwien.dsg.comot.m.adapter.general;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.state.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;

public abstract class Manager {

	protected final Logger log = LoggerFactory.getLogger(getClass());

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
	protected Processor processor;

	public void start(String participantId, Processor processor) throws Exception {
		this.participantId = participantId;
		this.processor = processor;

		processor.setManager(this);
		processor.start();

		start();

		log.info("started participant: '{}'", participantId);
	}

	public abstract void start();

	public abstract void clean();

	public abstract void removeInstanceListener(String instanceId) throws EpsException;

	public void sendLifeCycle(Type targetLevel, LifeCycleEvent event) throws AmqpException, JAXBException {

		event.setOrigin(getId());
		event.setTime(System.currentTimeMillis());

		String bindingKey = event.getCsInstanceId() + "." + LifeCycleEvent.class.getSimpleName() + "."
				+ event.getAction()
				+ "." + targetLevel;

		log.info(logId() + "EVENT-LC key={}", bindingKey);

		amqp.convertAndSend(Constants.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));
	}

	public void sendCustom(Type targetLevel, CustomEvent event) throws AmqpException, JAXBException {

		event.setOrigin(getId());
		event.setTime(System.currentTimeMillis());

		String bindingKey = event.getCsInstanceId() + "." + event.getClass().getSimpleName() + "."
				+ event.getCustomEvent() + "." + targetLevel;

		log.info(logId() + "EVENT-CUST key={}", bindingKey);

		amqp.convertAndSend(Constants.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));
	}

	public void sendException(String serviceId, String instanceId, Exception e) throws AmqpException, JAXBException {

		// log.info(logId() + "EVENT-EX key={}", "TODO");

		ExceptionMessage msg = new ExceptionMessage(serviceId, instanceId, getId(), System.currentTimeMillis(), e);

		String bindingKey = instanceId + "." + getId(); // TODO

		amqp.convertAndSend(Constants.EXCHANGE_EXCEPTIONS, bindingKey, Utils.asJsonString(msg));

	}

	protected String logId() {
		return "[" + participantId + "] ";
	}

	protected String getId() {
		return participantId;
	}
}
