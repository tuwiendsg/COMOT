package at.ac.tuwien.dsg.comot.m.core.adapter.general;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class SingleQueueManager extends Manager {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected SimpleMessageListenerContainer container;

	public void start() {

		admin.declareQueue(new Queue(queueName(), false, false, true));

		container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName());

		for (Binding binding : processor.getBindings(queueName(), null)) {
			admin.declareBinding(binding);
		}

		container.setMessageListener(new ProcessorListener(processor));
		container.start();
	}

	@PreDestroy
	public void clean() {

		if (container != null) {
			container.stop();
			container.shutdown();
		}
		// TODO clean processor?

		if (admin != null) {
			admin.deleteQueue(queueName());
		}

		log.debug("cleaned '{}'", queueName());
	}

	public String queueName() {
		return ADAPTER_QUEUE + participantId;
	}

	@Override
	public void removeInstanceListener(String instanceId) {
		// TODO Auto-generated method stub

	}

}
