package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.general;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import at.ac.tuwien.dsg.comot.m.common.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.core.UtilsLc;
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
			String groupId = msg.getEvent().getGroupId();
			String origin = msg.getEvent().getOrigin();

			if (msg.getEvent() instanceof LifeCycleEvent) {
				LifeCycleEvent event = (LifeCycleEvent) msg.getEvent();

				Action action = event.getAction();
				CloudService service = event.getService();
				Map<String, Transition> transitions = msg.getTransitions();

				log.info(logId() + "onLifecycleEvent: service={}, instance={}, group={}, action={}, origin={}",
						serviceId,
						instanceId, groupId, action, origin);

				onLifecycleEvent(msg, serviceId, instanceId, groupId, action, null, service, transitions);

			} else {

				CustomEvent event = (CustomEvent) msg.getEvent();

				String optionalMessage = event.getMessage();
				String eventName = event.getCustomEvent();
				String epsId = event.getEpsId();

				log.info(logId() + "onCustomEvent: service={}, instance={}, group={}, epsId={}, event={}, origin={}",
						serviceId, instanceId, groupId, epsId, eventName, origin);

				onCustomEvent(msg, serviceId, instanceId, groupId, eventName, epsId, optionalMessage);
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
			String epsId,
			String optionalMessage) throws Exception;

}
