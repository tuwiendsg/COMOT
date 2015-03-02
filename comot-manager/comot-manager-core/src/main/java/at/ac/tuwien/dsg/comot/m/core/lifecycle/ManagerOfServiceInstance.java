package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.EventMessage;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceEntity;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component("prototype")
public class ManagerOfServiceInstance {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected RabbitTemplate amqp;

	protected String csInstanceId;
	protected String serviceId;
	protected Group serviceGroup;
	protected Map<String, Group> groups = new HashMap<>();
	protected Map<String, State> lastStates = new HashMap<>();
	protected AggregationStrategy strategy = new AggregationStrategy();

	// TODO consider synchronized
	public void executeAction(EventMessage event) throws JAXBException, IOException {

		String groupId = event.getGroupId();
		Action action = event.getAction();
		CloudService service = event.getService();

		if (Action.NEW_INSTANCE_REQUESTED.equals(action)) {

			this.csInstanceId = event.getCsInstanceId();
			this.serviceId = service.getId();

			serviceGroup = new Group(service, strategy);

			for (Group group : serviceGroup.getAllMembersNested()) {
				groups.put(group.getId(), group);
				lastStates.put(group.getId(), State.NONE);
			}

		} else if (Action.DEPLOYMENT_REQUESTED.equals(action) && !groups.containsKey(groupId)) {

			for (ServiceUnit unit : Navigator.getAllUnits(service)) {
				for (UnitInstance instance : unit.getInstances()) {
					if (instance.getId().equals(groupId)) {

						Group newGroup = groups.get(unit.getId()).addInstance(instance);
						groups.put(newGroup.getId(), newGroup);
					}
				}
			}
		}

		Group group = groups.get(groupId);

		log.info("group check: {} {}", groupId, group);

		if (!group.canExecute(action)) {
			throw new ComotIllegalArgumentException("Action '" + action + "' is not allowed in state '"
					+ group.getCurrentState() + "'");
		}

		group.executeAction(action);

		// sum up transitions
		Map<String, State> tempStates = new HashMap<>();
		Set<Transition> transitions = new HashSet<>();
		boolean fresh;
		Group tempG;

		for (String key : groups.keySet()) {
			tempG = groups.get(key);
			fresh = true;
			tempStates.put(key, tempG.getCurrentState());
			if (tempG.getCurrentState().equals(lastStates.get(key))) {
				fresh = false;
			}
			transitions.add(new Transition(key, tempG.getType(), lastStates.get(key), tempG.getCurrentState(), fresh));
		}
		lastStates = tempStates;

		// enrich service with states
		Navigator nav = new Navigator(event.getService());

		for (ServiceEntity entity : nav.getAllServiceParts()) {
			if (groups.containsKey(entity.getId())) {
				entity.setState(groups.get(entity.getId()).getCurrentState());
			}
			if (entity.getClass().equals(ServiceUnit.class)) {
				for (UnitInstance instance : ((ServiceUnit) entity).getInstances()) {
					if (groups.containsKey(instance.getId())) {
						entity.setState(groups.get(entity.getId()).getCurrentState());
					}
				}
			}
		}

		// create binding
		//
		// instanceID.changeTRUE/FALSE.stateBefore.stateAfter.action.targetLevel
		//
		String change;

		if (serviceGroup.getCurrentState().equals(serviceGroup.getPreviousState())) {
			change = "FALSE";
		} else {
			change = "TRUE";
		}
		String targetLevel = groups.get(event.getGroupId()).getType().toString();

		String bindingKey = csInstanceId + "." + change + "." + serviceGroup.getPreviousState() + "."
				+ serviceGroup.getCurrentState() + "." + event.getAction() + "." + targetLevel;

		send(AppContextCore.EXCHANGE_LIFE_CYCLE, bindingKey, new StateMessage(event, transitions));

	}

	public void executeCustomAction(EventMessage event) throws AmqpException, JAXBException {

		String groupId = event.getGroupId();

		if (!groups.containsKey(groupId)) {
			throw new ComotIllegalArgumentException("The entity '" + groupId + "' of service instance '" + csInstanceId
					+ "' does not exist.");
		}

		Type targetType = groups.get(groupId).getType();
		String bindingKey = csInstanceId + "." + event.getCustomEvent() + "." + targetType;

		send(AppContextCore.EXCHANGE_CUSTOM_EVENT, bindingKey, new StateMessage(event));

	}

	protected void send(String exchange, String bindingKey, StateMessage message) throws AmqpException, JAXBException {

		log.info("SEND exchange={} key={}", exchange, bindingKey);

		amqp.convertAndSend(exchange, bindingKey, Utils.asJsonString(message));
	}

	public String getServiceId() {
		return serviceId;
	}

}
