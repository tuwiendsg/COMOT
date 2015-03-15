package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import at.ac.tuwien.dsg.comot.m.common.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.core.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;

public abstract class SingleQueueAdapter {

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

	@Autowired
	protected InformationServiceMock infoService;
	@Autowired
	protected LifeCycleManager lcManager;

	protected String adapterId;
	protected SimpleMessageListenerContainer container;
	protected List<Binding> bindings = new ArrayList<>();

	protected Set<String> managedSet = Collections.synchronizedSet(new HashSet<String>());

	public void setInfoService(InformationServiceMock infoService) {
		this.infoService = infoService;
	}

	public void startAdapter(String adapterId) {

		this.adapterId = adapterId;

		admin.declareQueue(new Queue(queueName(), false, false, false));

		container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName());

		start(adapterId);

		for (Binding binding : bindings) {
			admin.declareBinding(binding);
		}

		container.start();

		log.info("started adapter '{}'", adapterId);
	}

	@PreDestroy
	public void cleanAdapter() {

		if (container != null) {
			container.stop();
		}
		clean();

		for (Binding binding : bindings) {
			if (binding != null) {
				admin.removeBinding(binding);
			}
		}

		if (admin != null) {
			admin.deleteQueue(queueName());
		}

		log.debug("cleaned '{}'", queueName());
	}

	protected void bindingLifeCycle(String key) {
		bindings.add(new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_LIFE_CYCLE,
				key, null));
	}

	protected void bindingCustom(String key) {
		bindings.add(new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_CUSTOM_EVENT,
				key, null));
	}

	protected void sendLifeCycle(Type targetLevel, LifeCycleEvent event) throws AmqpException, JAXBException {

		String bindingKey = event.getCsInstanceId() + "." + event.getClass().getSimpleName() + "." + event.getAction()
				+ "." + targetLevel;

		// log.info(logId() +"SEND key={}", targetLevel);

		amqp.convertAndSend(AppContextCore.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));
	}

	protected void sendCustom(Type targetLevel, CustomEvent event) throws AmqpException, JAXBException {

		String bindingKey = event.getCsInstanceId() + "." + event.getClass().getSimpleName() + "."
				+ event.getCustomEvent() + "." + targetLevel;

		// log.info(logId() +"SEND key={}", targetLevel);

		amqp.convertAndSend(AppContextCore.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));
	}

	public String queueName() {
		return ADAPTER_QUEUE + adapterId;
	}

	protected abstract void start(String osuInstanceId);

	protected abstract void clean();

	protected String logId() {
		return "[" + adapterId + "] ";
	}
}
