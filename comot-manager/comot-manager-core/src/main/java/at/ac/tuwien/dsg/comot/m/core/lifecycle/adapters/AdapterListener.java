package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.UtilsLc;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;

public abstract class AdapterListener implements MessageListener {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected String adapterId;

	public AdapterListener() {

	}

	public AdapterListener(String adapterId) {
		super();
		this.adapterId = adapterId;
	}

	@Override
	public void onMessage(Message message) {

		try {

			StateMessage msg = UtilsLc.stateMessage(message);
			String instanceId = msg.getEvent().getCsInstanceId();
			String serviceId = msg.getEvent().getServiceId();
			String groupId = msg.getEvent().getServiceId();
			String optionalMessage = msg.getMessage();

			if (msg.getEvent().isLifeCycleDefined()) {

				Action action = msg.getEvent().getAction();
				CloudService service = msg.getEvent().getService();
				Map<String, Transition> transitions = msg.getTransitions();

				log.info(logId() + "onLifecycleEvent: service={}, instance={}, group={}, action={}", serviceId,
						instanceId, groupId, action);

				onLifecycleEvent(msg, serviceId, instanceId, groupId, action, optionalMessage, service, transitions);

			} else {

				String event = msg.getCustomEvent();

				log.info(logId() + "onCustomEvent: service={}, instance={}, group={}, event={}", serviceId, instanceId,
						groupId, event);

				onCustomEvent(msg, serviceId, instanceId, groupId, event, optionalMessage);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected String logId() {
		return "[" + adapterId + "] ";
	}

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
			String optionalMessage) throws Exception;

}
