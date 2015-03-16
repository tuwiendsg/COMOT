package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.general;

import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import at.ac.tuwien.dsg.comot.m.common.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;

public abstract class AdapterCore {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected RabbitTemplate amqp;

	protected AdapterManager manager;
	protected String adapterId;

	public void startAdapter(String adapterId) {

		this.adapterId = adapterId;
		start(adapterId);

		manager = context.getBean(AdapterManager.class);
		manager.start(this);

		log.info("started adapter '{}'", adapterId);
	}

	public String getId() {
		return adapterId;
	}

	public String logId() {
		return "[" + adapterId + "] ";
	}

	protected Binding bindingLifeCycle(String queueName, String key) {
		return new Binding(queueName, DestinationType.QUEUE, AppContextCore.EXCHANGE_LIFE_CYCLE,
				key, null);
	}

	protected Binding bindingCustom(String queueName, String key) {
		return new Binding(queueName, DestinationType.QUEUE, AppContextCore.EXCHANGE_CUSTOM_EVENT,
				key, null);
	}

	public void sendLifeCycle(Type targetLevel, LifeCycleEvent event) throws AmqpException, JAXBException {

		String bindingKey = event.getCsInstanceId() + "." + event.getClass().getSimpleName() + "." + event.getAction()
				+ "." + targetLevel;

		// log.info(logId() +"SEND key={}", targetLevel);

		amqp.convertAndSend(AppContextCore.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));
	}

	public void sendCustom(Type targetLevel, CustomEvent event) throws AmqpException, JAXBException {

		String bindingKey = event.getCsInstanceId() + "." + event.getClass().getSimpleName() + "."
				+ event.getCustomEvent() + "." + targetLevel;

		// log.info(logId() +"SEND key={}", targetLevel);

		amqp.convertAndSend(AppContextCore.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));
	}

	protected abstract void start(String osuInstanceId);

	public abstract List<Binding> getBindings(String queueName, String instanceId);

	protected abstract void onLifecycleEvent(
			StateMessage msg,
			String serviceId,
			String instanceId,
			String groupId,
			Action action,
			String optionalMessage,
			CloudService service,
			Map<String, Transition> transitions) throws Exception;

	protected abstract void onCustomEvent(
			StateMessage msg,
			String serviceId,
			String instanceId,
			String groupId,
			String event,
			String epsId,
			String optionalMessage) throws Exception;

}
