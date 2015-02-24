package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.EventMessage;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.Group.Type;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.model.type.Action;

@Component
public class LifeCycleManager {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected RabbitTemplate amqp;
	@Autowired
	protected AmqpAdmin admin;

	protected Map<String, ManagerOfServiceInstance> managers = new HashMap<>();

	public LifeCycleManager() {
	}

	@PostConstruct
	public void setUp() {

		admin.declareExchange(new TopicExchange(AppContextCore.EXCHANGE_INSTANCE_HIGH_LEVEL, false, false));
		admin.declareExchange(new TopicExchange(AppContextCore.EXCHANGE_INSTANCE_DETAILED, false, false));
		admin.declareExchange(new TopicExchange(AppContextCore.EXCHANGE_INSTANCE_CUSTOM, false, false));
	}

	public void executeAction(EventMessage event) throws IOException,
			JAXBException {

		log.info("executeAction( {})", event);

		String serviceId = event.getServiceId();
		String csInstanceId = event.getCsInstanceId();
		ManagerOfServiceInstance manager;

		if (Action.NEW_INSTANCE_REQUESTED.equals(event.getAction())) {

			if (managers.containsKey(csInstanceId)) {
				return;
			}

			manager = context.getBean(ManagerOfServiceInstance.class);
			managers.put(csInstanceId, manager);

			Map<String, Transition> transitions = manager.executeAction(event);

			sendToServiceBinding(serviceId, new StateMessage(event, transitions));

		} else if (Action.INSTANCE_REMOVAL_REQUESTED.equals(event.getAction())) {

			// TODO delete manager, remove exchanges

		} else {

			log.info("managers {}", managers);

			if (!managers.containsKey(csInstanceId)) {
				throw new ComotIllegalArgumentException("Instance '" + csInstanceId + "' has no managed life-cycle");
			}

			if (event.isLifeCycleDefined()) {

				manager = managers.get(csInstanceId);

				Map<String, Transition> transitions = manager.executeAction(event);
				StateMessage message = new StateMessage(event, transitions);

				// a change at service level occured
				if (transitions.containsKey(serviceId)
						&& !transitions.get(serviceId).getNewState().equals(transitions.get(serviceId).getOldState())) {

					sendToHighLevelBinding(csInstanceId, message);
				} else {
					sendToDetailedBinding(csInstanceId, message);
				}

			} else {
				sendToCustomEventsBinding(csInstanceId, new StateMessage(event));
			}
		}
	}

	protected void sendToServiceBinding(String serviceId, StateMessage message) throws AmqpException, JAXBException {

		String exchange = AppContextCore.EXCHANGE_SERVICES;
		String bindingKey = "" + message.getEvent().getAction();

		send(exchange, bindingKey, message);
	}

	protected void sendToHighLevelBinding(String csInstanceId, StateMessage message) throws AmqpException,
			JAXBException {

		Transition trans = message.getTransitions().get(managers.get(csInstanceId).getServiceId());

		String exchange = AppContextCore.EXCHANGE_INSTANCE_HIGH_LEVEL;
		String bindingKey = csInstanceId + "." + trans.getOldState() + "." + trans.getNewState();

		send(exchange, bindingKey, message);
	}

	protected void sendToDetailedBinding(String csInstanceId, StateMessage message) throws AmqpException, JAXBException {

		Type targetType = managers.get(csInstanceId).getGroups().get(message.getEvent().getGroupId()).getType();

		String exchange = AppContextCore.EXCHANGE_INSTANCE_DETAILED;
		String bindingKey = csInstanceId + "." + message.getEvent().getAction() + "." + targetType;

		send(exchange, bindingKey, message);
	}

	protected void sendToCustomEventsBinding(String csInstanceId, StateMessage message) throws AmqpException,
			JAXBException {

		Type targetType = managers.get(csInstanceId).getGroups().get(message.getEvent().getGroupId()).getType();

		String exchange = AppContextCore.EXCHANGE_INSTANCE_CUSTOM;
		String bindingKey = csInstanceId + "." + message.getEvent().getCustomEvent() + "." + targetType;

		send(exchange, bindingKey, message);

	}

	protected void send(String exchange, String bindingKey, StateMessage message) throws AmqpException, JAXBException {
		log.info("SEND exchange={} key={}", exchange, bindingKey);

		amqp.convertAndSend(exchange, bindingKey, Utils.asJsonString(message));
	}

	// public void executeAction(EventMessage event) throws IOException, JAXBException {
	//
	// for (ManagerOfServiceInstance manager : managers.values()) {
	// if (manager.getServiceGroup().getId().equals(serviceId)) {
	// executeAction(serviceId, manager.getServiceGroup().getId(), groupId, action);
	// }
	// }
	//
	// }

}
