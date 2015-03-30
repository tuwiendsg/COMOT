package at.ac.tuwien.dsg.comot.m.adapter.general;

import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import at.ac.tuwien.dsg.comot.m.adapter.UtilsLc;
import at.ac.tuwien.dsg.comot.m.common.events.ComotMessage;
import at.ac.tuwien.dsg.comot.m.common.events.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.events.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.events.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.events.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.events.Transition;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;

public class ProcessorListener implements MessageListener {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected Processor processor;

	public ProcessorListener(Processor processor) {
		this.processor = processor;
	}

	@Override
	public void onMessage(Message message) {

		String instanceId = null;
		String serviceId = null;

		try {

			ComotMessage comotMsg = UtilsLc.comotMessage(message);

			if (comotMsg instanceof StateMessage) {

				StateMessage msg = (StateMessage) comotMsg;
				instanceId = msg.getEvent().getCsInstanceId();
				serviceId = msg.getEvent().getServiceId();
				String groupId = msg.getEvent().getGroupId();
				String origin = msg.getEvent().getOrigin();

				if (msg.getEvent() instanceof LifeCycleEvent) {
					LifeCycleEvent event = (LifeCycleEvent) msg.getEvent();

					Action action = event.getAction();
					CloudService service = msg.getService();
					Map<String, Transition> transitions = msg.getTransitions();

					log.info(processor.logId()
							+ "onLifecycleEvent: service={}, instance={}, group={}, action={}, origin={}",
							serviceId, instanceId, groupId, action, origin);

					processor.onLifecycleEvent(msg, serviceId, instanceId, groupId, action, origin, service,
							transitions);

				} else {

					CustomEvent event = (CustomEvent) msg.getEvent();

					String optionalMessage = event.getMessage();
					String eventName = event.getCustomEvent();
					String epsId = event.getEpsId();

					log.info(processor.logId()
							+ "onCustomEvent: service={}, instance={}, group={}, epsId={}, event={}, origin={}",
							serviceId, instanceId, groupId, epsId, eventName, origin);

					processor.onCustomEvent(msg, serviceId, instanceId, groupId, eventName, epsId, origin,
							optionalMessage);
				}

			} else if (comotMsg instanceof ExceptionMessage) {
				ExceptionMessage msg = (ExceptionMessage) comotMsg;

				instanceId = msg.getCsInstanceId();
				serviceId = msg.getServiceId();
				String originId = msg.getOrigin();
				Exception e = msg.getException();

				log.info(processor.logId() + "onExceptionEvent: service={}, instance={}, origin={} exception={}",
						serviceId, instanceId, originId, e);

				processor.onExceptionEvent(msg, serviceId, instanceId, originId, e);
			}

		} catch (Exception e) {

			try {
				processor.getManager().sendException(serviceId, instanceId, e);
			} catch (AmqpException | JAXBException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}

	}

}
