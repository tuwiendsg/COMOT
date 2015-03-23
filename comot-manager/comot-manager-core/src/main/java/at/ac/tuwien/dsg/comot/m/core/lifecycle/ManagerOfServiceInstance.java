package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;
import java.util.Iterator;
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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.events.AbstractEvent;
import at.ac.tuwien.dsg.comot.m.common.events.ComotMessage;
import at.ac.tuwien.dsg.comot.m.common.events.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.events.ExceptionMessage;
import at.ac.tuwien.dsg.comot.m.common.events.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.events.ModifyingLifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.events.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.events.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotLifecycleException;
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
	protected RabbitTemplate amqp;
	@Autowired
	protected AmqpAdmin admin;
	@Autowired
	protected ConnectionFactory connectionFactory;
	protected SimpleMessageListenerContainer container;

	@Autowired
	protected LifeCycleManager parentManager;
	@Autowired
	protected InformationServiceMock infoService;
	@Autowired
	protected GroupManager groupManager;

	protected String csInstanceId;
	protected String serviceId;

	public String queueName() {
		return LC_MANAGER_QUEUE + csInstanceId;
	}

	public void createInstance(LifeCycleEvent event) throws ClassNotFoundException, AmqpException, IOException,
			JAXBException {

		this.csInstanceId = event.getCsInstanceId();
		this.serviceId = event.getServiceId();

		groupManager.setCsInstanceId(csInstanceId);
		groupManager.setServiceId(serviceId);

		admin.declareQueue(new Queue(queueName(), false, false, true));

		container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName());
		container.setPrefetchCount(0);
		container.setMessageListener(new CustomListener());

		admin.declareBinding(new Binding(queueName(), DestinationType.QUEUE, AppContextCore.EXCHANGE_REQUESTS,
				csInstanceId + ".#", null));

		try {
			executeLifeCycleEvent(event);
		} catch (ComotLifecycleException | ComotException e) {
			sendException(e);
		}

		container.start();

	}

	public class CustomListener implements MessageListener {

		@Override
		public void onMessage(Message message) {

			try {
				AbstractEvent event = UtilsLc.abstractEvent(message);

				log.info(logId() + " processing: {}", event);

				if (event instanceof LifeCycleEvent) {
					LifeCycleEvent incomingEvent = (LifeCycleEvent) event;
					if (incomingEvent.getAction() != Action.CREATED) {
						executeLifeCycleEvent(incomingEvent);
					}

				} else {
					executeCustomEvent((CustomEvent) event);
				}

			} catch (Exception e) {
				try {
					sendException(e);
					e.printStackTrace();
				} catch (AmqpException | JAXBException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public void executeLifeCycleEvent(LifeCycleEvent event)
			throws JAXBException, IOException, ClassNotFoundException, ComotLifecycleException, ComotException {

		String groupId = event.getGroupId();
		Action action = event.getAction();
		String change;
		Group targetGroup;
		CloudService service = null;
		ModifyingLifeCycleEvent modEvent = null;

		if (event instanceof ModifyingLifeCycleEvent) {
			modEvent = (ModifyingLifeCycleEvent) event;
		}

		try {

			if (Action.CREATED == action) {

				if (serviceId.equals(groupId)) { // SERVICE

					service = infoService.getServiceInstance(serviceId, csInstanceId);
					groupManager.createGroupService(action, service);

				} else { // TOPOLOGY or UNIT

					if (modEvent == null) {
						throw new ComotException("The event '" + event + "' should be of type: "
								+ ModifyingLifeCycleEvent.class);
					}

					groupManager.createGroupUnitTopo(action, modEvent.getParentId(), modEvent.getEntity());
					// TODO adapt the information model
				}

			} else if (Action.DEPLOYMENT_STARTED == action) {

				if (!groupManager.containsGroup(groupId)) { // INSTANCES

					if (modEvent == null) {
						throw new ComotException("The event '" + event + "' should be of type: "
								+ ModifyingLifeCycleEvent.class);
					}

					groupManager.createGroupInstance(action, modEvent.getParentId(), modEvent.getInstance());
					infoService
							.addUnitInstance(serviceId, csInstanceId, modEvent.getParentId(), modEvent.getInstance());

				} else { // ALL OTHER
					groupManager.checkAndExecute(action, groupId);
				}

			} else if (Action.DEPLOYED.equals(action)) {

				targetGroup = groupManager.checkAndExecute(action, groupId);

				if (targetGroup.getType() == Type.INSTANCE) {

					if (modEvent == null) {
						throw new ComotException("The event '" + event + "' should be of type: "
								+ ModifyingLifeCycleEvent.class);
					}

					log.info("updating instance: {}", targetGroup.getId());
					infoService.updateUnitInstance(serviceId, csInstanceId, modEvent.getInstance());

				} else {
					groupManager.checkAndExecute(action, groupId);
				}

			} else if (Action.UNDEPLOYED == action) {

				for (Group temp : groupManager.getGroup(serviceId).getAllMembersNested()) {
					if (temp.getCurrentState() == State.FINAL && temp.getType() == Type.INSTANCE) {
						infoService.removeUnitInstance(serviceId, csInstanceId, temp.getId());
					}
				}

				groupManager.checkAndExecute(action, groupId);

			} else if (Action.REMOVED == action) {

				if (serviceId.equals(groupId)) { // SERVICE
					service = infoService.getServiceInstance(serviceId, csInstanceId);
					infoService.removeServiceInstance(serviceId, csInstanceId);
					// parentManager.removeInstanceManager(this);

				} else {
					// TODO adapt the information model
				}

				groupManager.checkAndExecute(action, groupId);

			} else if (Action.MOVE == action) {

				// TODO

				groupManager.checkAndExecute(action, groupId);

			} else {
				groupManager.checkAndExecute(action, groupId);
			}

			State currentState = groupManager.getGroup(serviceId).getCurrentState();
			State previousState = groupManager.getGroup(serviceId).getPreviousState();

			// create binding
			if (currentState == previousState) {
				change = "FALSE";
			} else {
				change = "TRUE";
			}

			String bindingKey = csInstanceId + "." + change + "." + previousState + "." + currentState + "." + action
					+ "." + groupManager.getGroup(groupId).getType() + "." + event.getOrigin();

			// clean service
			if (service == null) {
				service = infoService.getServiceInstance(serviceId, csInstanceId);
			}
			service = UtilsLc.removeProviderInfo(service);

			StateMessage message = new StateMessage(event, groupManager.extractTransitions(event), service);

			send(AppContextCore.EXCHANGE_LIFE_CYCLE, bindingKey, message);

			// remove groups

			for (Iterator<Group> iterator = groupManager.getGroup(serviceId).getAllMembersNested().iterator(); iterator
					.hasNext();) {
				Group temp = iterator.next();
				if (temp.getCurrentState() == State.FINAL) {
					temp.getParent().removeMemberNested(temp.getId());
				}
			}

		} catch (ComotLifecycleException e) {
			e.setEvent(event);
			throw e;
		}
	}

	public void executeCustomEvent(CustomEvent event) throws AmqpException, JAXBException, ClassNotFoundException,
			IOException, ComotException {

		String groupId = event.getGroupId();

		if (!groupManager.containsGroup(groupId)) {
			throw new ComotException("The entity '" + groupId + "' of service instance '" + csInstanceId
					+ "' does not exist.");
		}

		// create binding
		String bindingKey = csInstanceId + "." + event.getEpsId() + "." + event.getCustomEvent() + "."
				+ groupManager.getGroup(groupId).getType();

		CloudService service = infoService.getServiceInstance(serviceId, csInstanceId);

		send(AppContextCore.EXCHANGE_CUSTOM_EVENT, bindingKey,
				new StateMessage(event, groupManager.extractTransitions(event), service));

	}

	protected void sendException(Exception e) throws AmqpException, JAXBException {

		send(AppContextCore.EXCHANGE_EXCEPTIONS, csInstanceId + "." + csInstanceId, new ExceptionMessage(serviceId,
				csInstanceId, csInstanceId, e));

	}

	protected void send(String exchange, String bindingKey, ComotMessage message) throws AmqpException, JAXBException {

		log.info(logId() + "STAT-EVENT exchange={} key={}", exchange, bindingKey);
		amqp.convertAndSend(exchange, bindingKey, Utils.asJsonString(message));
	}

	public State getCurrentState(String groupId) {
		return groupManager.getCurrentState(groupId);
	}

	public State getCurrentStateService() {
		return groupManager.getCurrentState(serviceId);
	}

	public Map<String, Transition> getCurrentState() {
		return groupManager.getCurrentState();
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
	 * @throws JAXBException
	 * @throws ComotLifecycleException
	 * @throws ComotException
	 */
	public void hardSetRunning(String instanceId, CloudService service) throws ClassNotFoundException, IOException,
			JAXBException, ComotLifecycleException, ComotException {

		for (ServiceUnit unit : Navigator.getAllUnits(service)) {
			for (UnitInstance uInst : unit.getInstances()) {
				groupManager.createGroupInstance(Action.DEPLOYMENT_STARTED, unit.getId(), uInst);
				infoService.addUnitInstance(serviceId, csInstanceId, unit.getId(), uInst);
			}
		}
		
		groupManager.checkAndExecute(Action.DEPLOYED, serviceId);

		for (Group group : groupManager.getGroup(serviceId).getAllMembersNested()) {
			group.currentState = State.RUNNING;
		}
	}
}
