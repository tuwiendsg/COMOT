package at.ac.tuwien.dsg.comot.m.adapter.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.UtilsLc;
import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;

@Component
@Scope("prototype")
public class PerInstanceQueueManager extends Manager {

	@Autowired
	protected InformationClient infoService;

	protected SimpleMessageListenerContainer container;
	protected Map<String, SimpleMessageListenerContainer> containers = new HashMap<>();

	protected List<Binding> bindings = new ArrayList<>();

	public void start() {

		startAssignmentListener();
	}

	protected void startAssignmentListener() {

		admin.declareQueue(new Queue(queueNameAssignment(), false, false, true));

		admin.declareBinding(new Binding(queueNameAssignment(), DestinationType.QUEUE,
				Constants.EXCHANGE_CUSTOM_EVENT,
				"*." + participantId + "." + EpsEvent.EPS_SUPPORT_REQUESTED + "." + Type.SERVICE, null));

		admin.declareBinding(new Binding(queueNameAssignment(), DestinationType.QUEUE,
				Constants.EXCHANGE_CUSTOM_EVENT,
				"*.*." + EpsEvent.EPS_SUPPORT_REMOVED + "." + Type.SERVICE, null));

		container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueNameAssignment());
		container.setMessageListener(new AssignmentListener());
		container.start();

	}

	protected void startServiceInstanceListener(String instanceId) {

		admin.declareQueue(new Queue(queueNameInstance(instanceId), false, false, true));

		for (Binding binding : processor.getBindings(queueNameInstance(instanceId), instanceId)) {
			admin.declareBinding(binding);
		}

		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueNameInstance(instanceId));
		container.setMessageListener(new ProcessorListener(processor));
		container.start();

		containers.put(instanceId, container);
	}

	public class AssignmentListener implements MessageListener {

		@Override
		public void onMessage(Message message) {

			try {
				CustomEvent event = (CustomEvent) UtilsLc.stateMessage(message).getEvent();
				String serviceId = event.getServiceId();
				String groupId = event.getGroupId();
				String action = event.getCustomEvent();

				if (action.equals(EpsEvent.EPS_SUPPORT_REQUESTED.toString())) {

					if (!containers.containsKey(serviceId)) {

						startServiceInstanceListener(serviceId);

						log.info("oooooooooooo {} {} ", serviceId, participantId);

						infoService.assignEps(serviceId, participantId);

						sendCustom(
								Type.SERVICE,
								new CustomEvent(serviceId, groupId, EpsEvent.EPS_SUPPORT_ASSIGNED.toString(),
										participantId, null));
					}

				} else if (action.equals(EpsEvent.EPS_SUPPORT_REMOVED.toString())) {
					removeInstanceListener(serviceId);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void removeInstanceListener(String serviceId) throws EpsException {

		if (containers.containsKey(serviceId)) {

			infoService.removeEpsAssignment(serviceId, participantId);

			SimpleMessageListenerContainer container = containers.get(serviceId);
			if (container != null) {
				container.stop();
				container.shutdown();
			}

			if (admin != null) {
				admin.deleteQueue(queueNameInstance(serviceId));
			}
		}
	}

	public String queueNameAssignment() {
		return queueNameAssignment(participantId);
	}

	public static String queueNameAssignment(String adapterId) {
		return ADAPTER_QUEUE + adapterId;
	}

	public String queueNameInstance(String instanceId) {
		return ADAPTER_QUEUE + participantId + "_" + instanceId;
	}

	@PreDestroy
	public void clean() {

		if (container != null) {
			container.stop();
			container.shutdown();
		}

		if (admin != null) {
			admin.deleteQueue(queueNameAssignment());
		}

		for (String instanceId : containers.keySet()) {
			try {
				removeInstanceListener(instanceId);
			} catch (EpsException e) {
				e.printStackTrace();
			}
		}

	}

}
