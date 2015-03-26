package at.ac.tuwien.dsg.comot.m.adapter.general;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;

import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.events.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.events.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.events.Transition;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;

public abstract class Processor {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected Manager manager;

	public void setManager(Manager manager) {
		this.manager = manager;
	}

	public Manager getManager() {
		return manager;
	}

	public Binding bindingLifeCycle(String queueName, String key) {
		return new Binding(queueName, DestinationType.QUEUE, Constants.EXCHANGE_LIFE_CYCLE,
				key, null);
	}

	public Binding bindingCustom(String queueName, String key) {
		return new Binding(queueName, DestinationType.QUEUE, Constants.EXCHANGE_CUSTOM_EVENT,
				key, null);
	}

	public Binding bindingException(String queueName, String key) {
		return new Binding(queueName, DestinationType.QUEUE, Constants.EXCHANGE_EXCEPTIONS,
				key, null);
	}

	public abstract List<Binding> getBindings(String queueName, String instanceId);

	public void start() throws Exception {

	}

	public abstract void onLifecycleEvent(
			StateMessage msg,
			String serviceId,
			String instanceId,
			String groupId,
			Action action,
			String originId,
			CloudService service,
			Map<String, Transition> transitions) throws Exception;

	public abstract void onCustomEvent(
			StateMessage msg,
			String serviceId,
			String instanceId,
			String groupId,
			String event,
			String epsId,
			String originId,
			String optionalMessage) throws Exception;

	public abstract void onExceptionEvent(
			ExceptionMessage msg,
			String serviceId,
			String instanceId,
			String originId,
			Exception e) throws Exception;

	public String logId() {
		return manager.logId();
	}

	public String getId() {
		return manager.getId();
	}

}
