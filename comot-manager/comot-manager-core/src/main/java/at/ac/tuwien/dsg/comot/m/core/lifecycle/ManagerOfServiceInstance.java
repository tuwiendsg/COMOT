package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.AbstractEvent;
import at.ac.tuwien.dsg.comot.m.common.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.m.core.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.UtilsLc;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
@Scope("prototype")
public class ManagerOfServiceInstance {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static final String LC_MANAGER_QUEUE = "LC_MANAGER_QUEUE_";

	@Autowired
	protected ApplicationContext context;

	@Autowired
	protected RabbitTemplate amqp;
	@Autowired
	protected AmqpAdmin admin;
	@Autowired
	protected InformationServiceMock infoService;
	@Autowired
	protected ConnectionFactory connectionFactory;
	protected SimpleMessageListenerContainer container;

	protected String csInstanceId;
	protected String serviceId;
	protected Group serviceGroup;
	protected Group serviceGroupReadOnly;
	protected Map<String, State> lastStates = new HashMap<>();
	protected AggregationStrategy strategy = new AggregationStrategy();

	public String queueName() {
		return LC_MANAGER_QUEUE + csInstanceId;
	}

	public void createInstance(LifeCycleEvent event) throws ClassNotFoundException, AmqpException, IOException,
			JAXBException {

		this.csInstanceId = event.getCsInstanceId();
		this.serviceId = event.getServiceId();

		admin.declareQueue(new Queue(queueName(), false, false, false));

		container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName());
		container.setMessageListener(new CustomListener());

		admin.declareBinding(new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_REQUESTS,
				csInstanceId + ".#", null));

		container.start();

		executeAction(event);

	}

	public class CustomListener implements MessageListener {

		@Override
		public void onMessage(Message message) {

			try {
				AbstractEvent event = UtilsLc.abstractEvent(message);

				log.info(logId() + " processing: {}", event);

				if (event instanceof LifeCycleEvent) {
					executeAction((LifeCycleEvent) event);
				} else {
					executeCustomAction((CustomEvent) event);
				}

			} catch (JAXBException | ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}

		}

	}

	protected void executeAction(LifeCycleEvent event)
			throws JAXBException, IOException, ClassNotFoundException {

		String groupId = event.getGroupId();
		Action action = event.getAction();
		CloudService service = event.getService();
		Group targetGroup;
		boolean found = false;

		if (Action.REMOVED != event.getAction()) {
			// clean service
			if (event.getService() == null) {
				service = infoService.getServiceInstance(serviceId, csInstanceId);
			} else {
				service = (CloudService) Utils.deepCopy(event.getService());
			}
			event.setService(UtilsLc.removeProviderInfo(service));
		}

		if (Action.CREATED.equals(action)) {

			serviceGroup = new Group(service, State.INIT);

			for (Group group : serviceGroup.getAllMembersNested()) {
				lastStates.put(group.getId(), State.INIT);
			}

			targetGroup = checkAndExecute(action, groupId);

		} else if (Action.DEPLOYMENT_STARTED.equals(action) && serviceGroup.getMemberNested(groupId) == null) {
			log.info("creating new group {}", service);

			// add new instance groups
			for (ServiceUnit unit : Navigator.getAllUnits(service)) {
				for (UnitInstance instance : unit.getInstances()) {
					if (instance.getId().equals(groupId)) {
						Group newGroup = serviceGroup.getMemberNested(unit.getId()).addGroup(instance.getId(),
								Type.INSTANCE, State.INIT);
						lastStates.put(newGroup.getId(), State.PASSIVE);

						infoService.addUnitInstance(serviceId, csInstanceId, unit.getId(), instance);

						log.info("newGroup: {}", newGroup);
						found = true;
						break;
					}
				}
				if (found) {
					break;
				}
			}

			targetGroup = checkAndExecute(action, groupId);

		} else if (Action.DEPLOYED.equals(action)) {

			targetGroup = checkAndExecute(action, groupId);

			if (targetGroup.getType() == Type.INSTANCE) {
				log.info("updating instance: {}", targetGroup.getId());
				UnitInstance instance = new Navigator(service).getInstance(targetGroup.getId());
				infoService.updateUnitInstance(serviceId, csInstanceId, instance);
			}

		} else if (Action.UNDEPLOYED.equals(action)) {

			// remove new instance groups
			targetGroup = checkAndExecute(action, groupId);

			for (Group member : targetGroup.getAllMembersNested()) {
				if (member.getType() == Type.INSTANCE) {
					infoService.removeUnitInstance(serviceId, csInstanceId, member.getId());
					member.getParent().getMembers().remove(member);
				}
			}

		} else if (Action.ELASTIC_CHANGE_FINISHED.equals(action)) {

			targetGroup = checkAndExecute(action, groupId);
		} else if (Action.UPDATE_FINISHED.equals(action)) {

			targetGroup = checkAndExecute(action, groupId);

			// } else if (Action.ERROR.equals(action)) {
			//
			// // TODO process ERROR
			// targetGroup = serviceGroup.getMemberNested(groupId);

		} else {
			targetGroup = checkAndExecute(action, groupId);
		}

		processEvent(event, targetGroup);
	}

	protected void processEvent(LifeCycleEvent event, Group targetGroup) throws ClassNotFoundException, IOException,
			AmqpException, JAXBException {

		// sum up transitions
		Map<String, State> tempStates = new HashMap<>();
		Map<String, Transition> transitions = new HashMap<>();
		boolean fresh;

		for (Group tempG : serviceGroup.getAllMembersNested()) {
			tempStates.put(tempG.getId(), tempG.getCurrentState());
			if (tempG.getCurrentState().equals(lastStates.get(tempG.getId()))) {
				fresh = false;
			} else {
				fresh = true;
			}
			transitions.put(tempG.getId(), new Transition(tempG.getId(), tempG.getType(), tempG.getPreviousState(),
					tempG.getCurrentState(), fresh));
		}
		lastStates = tempStates;
		serviceGroupReadOnly = (Group) Utils.deepCopy(serviceGroup);

		// create binding
		String change;

		if (serviceGroup.getCurrentState().equals(serviceGroup.getPreviousState())) {
			change = "FALSE";
		} else {
			change = "TRUE";
		}

		String bindingKey = csInstanceId + "." + change + "." + serviceGroup.getPreviousState() + "."
				+ serviceGroup.getCurrentState() + "." + event.getAction() + "." + targetGroup.getType();

		send(AppContextCore.EXCHANGE_LIFE_CYCLE, bindingKey, new StateMessage(event, transitions));

	}

	protected void executeCustomAction(CustomEvent event) throws AmqpException, JAXBException {

		String groupId = event.getGroupId();
		Group group;

		if ((group = serviceGroup.getMemberNested(groupId)) == null) {
			throw new ComotIllegalArgumentException("The entity '" + groupId + "' of service instance '" + csInstanceId
					+ "' does not exist.");
		}

		// sum up transitions
		Map<String, Transition> transitions = new HashMap<>();

		for (Group tempG : serviceGroup.getAllMembersNested()) {
			transitions.put(tempG.getId(), new Transition(
					tempG.getId(), tempG.getType(), tempG.getPreviousState(), tempG.getCurrentState(), false));
		}

		// create binding
		String bindingKey = csInstanceId + "." + event.getEpsId() + "." + event.getCustomEvent() + "."
				+ group.getType();

		send(AppContextCore.EXCHANGE_CUSTOM_EVENT, bindingKey, new StateMessage(event, transitions));

	}

	protected Group checkAndExecute(Action action, String groupId) {

		Group group = serviceGroup.getMemberNested(groupId);

		log.info("[Manager_{}] group check: {} {}", csInstanceId, groupId, group);
		log.info("getCurrentState(instanceId={}, groupId={}): {}", csInstanceId, groupId, serviceGroup);

		if (!group.canExecute(action)) {
			// TODO handle errors, this causes endless loop
			throw new ComotIllegalArgumentException("Action '" + action + "' is not allowed in state '"
					+ group.getCurrentState() + "'. Group " + groupId);
		}

		group.executeAction(action, strategy);

		return group;
	}

	protected void send(String exchange, String bindingKey, StateMessage message) throws AmqpException, JAXBException {

		log.info(logId() + "SEND exchange={} key={}", exchange, bindingKey);

		amqp.convertAndSend(exchange, bindingKey, Utils.asJsonString(message));
	}

	public String getServiceId() {
		return serviceId;
	}

	public State getCurrentState(String groupId) {

		if (serviceGroupReadOnly == null || serviceGroupReadOnly.getMemberNested(groupId) == null) {
			return null;
		}
		final State temp = serviceGroupReadOnly.getMemberNested(groupId).getCurrentState();

		log.info("getCurrentState(instanceId={}, groupId={}): {}", csInstanceId, groupId, temp);

		return temp;
	}

	public State getCurrentStateService() {
		return getCurrentState(serviceId);
	}

	public Map<String, Transition> getCurrentState() {
		Map<String, Transition> transitions = new HashMap<>();

		if (serviceGroupReadOnly == null) {
			return null;
		}

		for (Group tempG : serviceGroupReadOnly.getAllMembersNested()) {
			transitions.put(tempG.getId(), new Transition(tempG.getId(), tempG.getType(), tempG.getPreviousState(),
					tempG.getCurrentState(), false));
		}
		return transitions;
	}

	@PreDestroy
	public void clean() {

		if (container != null) {
			container.stop();
		}

		if (admin != null) {
			admin.deleteQueue(queueName());
		}

	}

	public String logId() {
		return "[ MANAGER_" + csInstanceId + "] ";
	}

	/**
	 * Only for testing!
	 * 
	 * @param instanceId
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void hardSetRunning(String instanceId, CloudService service) throws ClassNotFoundException, IOException {

		this.csInstanceId = instanceId;
		this.serviceId = service.getId();

		serviceGroup = new Group(service, State.INIT);

		for (Group group : serviceGroup.getAllMembersNested()) {
			group.currentState = State.RUNNING;
		}

		serviceGroupReadOnly = (Group) Utils.deepCopy(serviceGroup);
	}

}
