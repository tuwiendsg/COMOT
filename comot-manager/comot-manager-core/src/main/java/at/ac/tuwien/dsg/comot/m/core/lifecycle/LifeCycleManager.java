package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.m.core.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.UtilsLc;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.RecordingAdapter;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
public class LifeCycleManager {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static final String MANAGER_QUEUE = "MANAGER_QUEUE";

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected AmqpAdmin admin;
	@Autowired
	protected ConnectionFactory connectionFactory;
	protected SimpleMessageListenerContainer container;

	@Autowired
	protected InformationServiceMock infoService;

	protected Map<String, ManagerOfServiceInstance> managers = new HashMap<>();

	public LifeCycleManager() {
	}

	@PostConstruct
	public void setUp() {

		admin.declareExchange(new TopicExchange(AppContextCore.EXCHANGE_LIFE_CYCLE, false, false));
		admin.declareExchange(new TopicExchange(AppContextCore.EXCHANGE_CUSTOM_EVENT, false, false));
		admin.declareExchange(new TopicExchange(AppContextCore.EXCHANGE_REQUESTS, false, false));

		admin.declareQueue(new Queue(MANAGER_QUEUE, false, false, false));

		container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(MANAGER_QUEUE);
		container.setMessageListener(new CustomListener());

		admin.declareBinding(new Binding(MANAGER_QUEUE, DestinationType.QUEUE, AppContextCore.EXCHANGE_REQUESTS,
				"*." + LifeCycleEvent.class.getSimpleName() + "." + Action.CREATED + "." + Type.SERVICE, null));
		admin.declareBinding(new Binding(MANAGER_QUEUE, DestinationType.QUEUE, AppContextCore.EXCHANGE_REQUESTS,
				"*." + LifeCycleEvent.class.getSimpleName() + "." + Action.REMOVED + "." + Type.SERVICE, null));

		container.start();

		RecordingAdapter recording = context.getBean(RecordingAdapter.class);
		recording.startAdapter(InformationServiceMock.RECORDER);

	}

	public class CustomListener implements MessageListener {

		@Override
		public void onMessage(Message message) {

			try {
				LifeCycleEvent event = (LifeCycleEvent) UtilsLc.abstractEvent(message);
				String csInstanceId = event.getCsInstanceId();

				if (Action.CREATED == event.getAction()) {

					if (managers.containsKey(csInstanceId)) {
						return;
					}

					ManagerOfServiceInstance manager = context.getBean(ManagerOfServiceInstance.class);
					managers.put(csInstanceId, manager);

					manager.createInstance(event);

				} else if (Action.REMOVED == event.getAction()) {

					managers.remove(managers.get(csInstanceId));
				}

			} catch (JAXBException | ClassNotFoundException | AmqpException | IOException e) {
				e.printStackTrace();
			}
		}

	}

	public State getCurrentState(String instanceId, String groupId) {
		return managers.get(instanceId).getCurrentState(groupId);
	}

	public State getCurrentStateService(String instanceId) {
		return managers.get(instanceId).getCurrentStateService();

	}

	public Map<String, Transition> getCurrentState(String instanceId) {
		if (managers.containsKey(instanceId)) {
			return managers.get(instanceId).getCurrentState();
		} else {
			return null;
		}
	}

	public boolean isInstanceManaged(String instanceId) {
		if (managers.containsKey(instanceId)) {
			return true;
		} else {
			return false;
		}
	}

	@PreDestroy
	public void clean() {

		if (container != null) {
			container.stop();
		}

		if (admin != null) {
			admin.deleteQueue(MANAGER_QUEUE);
		}

	}

	/**
	 * Only for testing!
	 * 
	 * @param instanceId
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void hardSetRunning(CloudService service, String instanceId) throws ClassNotFoundException, IOException {

		ManagerOfServiceInstance manager = context.getBean(ManagerOfServiceInstance.class);
		managers.put(instanceId, manager);

		manager.hardSetRunning(instanceId, service);
	}
}
