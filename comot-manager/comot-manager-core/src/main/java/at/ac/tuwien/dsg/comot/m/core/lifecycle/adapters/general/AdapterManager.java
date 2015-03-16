package at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.general;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.EpsAction;
import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.m.core.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.UtilsLc;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;

@Component
@Scope("prototype")
public class AdapterManager {

	public static final String ADAPTER_QUEUE = "ADAPTER_QUEUE_";

	@Autowired
	protected ApplicationContext context;

	@Autowired
	protected AmqpAdmin admin;
	@Autowired
	protected ConnectionFactory connectionFactory;
	@Autowired
	protected InformationServiceMock infoService;

	protected SimpleMessageListenerContainer container;
	protected Map<String, SimpleMessageListenerContainer> containers = new HashMap<>();

	protected AdapterCore adapter;

	public void start(AdapterCore adapter) {
		this.adapter = adapter;

		startAssignmentListener();
	}

	protected void startAssignmentListener() {

		// TODO queue is created by EPS coordinator
		// admin.declareQueue(new Queue(queueNameAssignment(), false, false, false));

		admin.declareBinding(new Binding(queueNameAssignment(), DestinationType.QUEUE,
				AppContextCore.EXCHANGE_CUSTOM_EVENT,
				"*." + adapter.getId() + "." + EpsAction.EPS_ASSIGNMENT_REQUESTED + "." + Type.SERVICE, null));

		container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueNameAssignment());
		container.setMessageListener(new AssignmentListener());
		container.start();

	}

	protected void startServiceInstanceListener(String instanceId) {

		admin.declareQueue(new Queue(queueNameInstance(instanceId), false, false, false));

		for (Binding binding : adapter.getBindings(queueNameInstance(instanceId), instanceId)) {
			admin.declareBinding(binding);
		}

		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueNameInstance(instanceId));
		container.setMessageListener(new InstanceListener(adapter));
		container.start();

		containers.put(instanceId, container);
	}

	public class AssignmentListener implements MessageListener {

		@Override
		public void onMessage(Message message) {

			try {
				CustomEvent event = (CustomEvent) UtilsLc.stateMessage(message).getEvent();
				String instanceId = event.getCsInstanceId();
				String serviceId = event.getServiceId();
				String groupId = event.getGroupId();

				startServiceInstanceListener(instanceId);

				infoService.assignSupportingService(serviceId, instanceId, adapter.getId());

				adapter.sendCustom(Type.SERVICE,
						new CustomEvent(serviceId, instanceId, groupId, EpsAction.EPS_ASSIGNED.toString(),
								adapter.getId(), adapter.getId(), null));

			} catch (UnsupportedEncodingException | JAXBException e) {
				e.printStackTrace();
			}
		}
	}

	public void removeInstanceListener(String instanceId) {

		infoService.removeAssignmentOfSupportingOsu(instanceId, adapter.getId());

		SimpleMessageListenerContainer container = containers.get(instanceId);
		if (container != null) {
			container.stop();
		}

		if (admin != null) {
			admin.deleteQueue(queueNameInstance(instanceId));
		}
	}

	public String queueNameAssignment() {
		return queueNameAssignment(adapter.getId());
	}

	public static String queueNameAssignment(String adapterId) {
		return ADAPTER_QUEUE + adapterId;
	}

	public String queueNameInstance(String instanceId) {
		return ADAPTER_QUEUE + adapter.getId() + "_" + instanceId;
	}

	@PreDestroy
	public void clean() {

		if (container != null) {
			container.stop();
		}

		if (admin != null) {
			admin.deleteQueue(queueNameAssignment());
		}

		for (String instanceId : containers.keySet()) {
			removeInstanceListener(instanceId);
		}

	}
}