package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;

public abstract class Adapter {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static final String ADAPTER_QUEUE = "ADAPTER_QUEUE_";

	@Autowired
	protected AmqpAdmin admin;
	@Autowired
	protected ConnectionFactory connectionFactory;

	@Autowired
	protected InformationServiceMock infoService;
	@Autowired
	protected LifeCycleManager lcManager;

	protected String adapterId;
	protected SimpleMessageListenerContainer container;
	protected List<Binding> bindings = new ArrayList<>();

	protected Set<String> managedSet = Collections.synchronizedSet(new HashSet<String>());

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

	public String queueName() {
		return ADAPTER_QUEUE + adapterId;
	}

	protected abstract void start(String osuInstanceId);

	protected abstract void clean();

	protected String logId() {
		return "[" + adapterId + "] ";
	}
}
