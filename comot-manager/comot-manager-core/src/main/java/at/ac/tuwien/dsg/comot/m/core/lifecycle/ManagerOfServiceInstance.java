package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component("prototype")
public class ManagerOfServiceInstance {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected RabbitTemplate amqp;
	@Autowired
	protected InformationServiceMock infoService;

	protected String csInstanceId;
	protected String serviceId;
	protected Group serviceGroup;
	protected Group serviceGroupReadOnly;
	protected Map<String, State> lastStates = new HashMap<>();
	protected AggregationStrategy strategy = new AggregationStrategy();

	synchronized public void executeAction(
			EventMessage event) throws JAXBException, IOException, ClassNotFoundException {

		String groupId = event.getGroupId();
		Action action = event.getAction();
		CloudService service = event.getService();
		Group targetGroup;
		boolean found = false;

		if (Action.CREATED.equals(action)) {

			this.csInstanceId = event.getCsInstanceId();
			this.serviceId = event.getServiceId();

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

		// processOutgoingService(event);

		// sum up transitions
		Map<String, State> tempStates = new HashMap<>();
		Map<String, Transition> transitions = new HashMap<>();
		boolean fresh;

		for (Group tempG : serviceGroup.getAllMembersNested()) {
			fresh = true;
			tempStates.put(tempG.getId(), tempG.getCurrentState());
			if (tempG.getCurrentState().equals(lastStates.get(tempG.getId()))) {
				fresh = false;
			}
			transitions.put(tempG.getId(), new Transition(tempG.getId(), tempG.getType(), tempG.getPreviousState(),
					tempG.getCurrentState(), fresh));
		}
		lastStates = tempStates;
		serviceGroupReadOnly = (Group) Utils.deepCopy(serviceGroup);

		// create binding
		// instanceID.changeTRUE/FALSE.stateBefore.stateAfter.action.targetLevel
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

	protected Group checkAndExecute(Action action, String groupId) {

		Group group = serviceGroup.getMemberNested(groupId);

		log.info("[Manager_{}] group check: {} {}", csInstanceId, groupId, group);
		log.info("getCurrentState(instanceId={}, groupId={}): {}", csInstanceId, groupId, serviceGroup);

		if (!group.canExecute(action)) {
			throw new ComotIllegalArgumentException("Action '" + action + "' is not allowed in state '"
					+ group.getCurrentState() + "'. Group " + groupId);
		}

		group.executeAction(action, strategy);

		return group;
	}

	// protected void processOutgoingService(EventMessage event) throws ClassNotFoundException, IOException {
	//
	// CloudService service = event.getService();
	// service = infoService.getServiceInstance(serviceId, csInstanceId);
	// event.setService(UtilsLc.removeProviderInfo((CloudService) Utils.deepCopy(service)));
	// }

	public void executeCustomAction(EventMessage event) throws AmqpException, JAXBException {

		String groupId = event.getGroupId();
		Group group;

		if ((group = serviceGroup.getMemberNested(groupId)) == null) {
			throw new ComotIllegalArgumentException("The entity '" + groupId + "' of service instance '" + csInstanceId
					+ "' does not exist.");
		}

		String bindingKey = csInstanceId + "." + event.getCustomEvent() + "." + group.getType();

		send(AppContextCore.EXCHANGE_CUSTOM_EVENT, bindingKey, new StateMessage(event));

	}

	protected void send(String exchange, String bindingKey, StateMessage message) throws AmqpException, JAXBException {

		log.info("SEND exchange={} key={}", exchange, bindingKey);

		amqp.convertAndSend(exchange, bindingKey, Utils.asJsonString(message));
	}

	public String getServiceId() {
		return serviceId;
	}

	public State getCurrentState(String groupId) {
		log.info("getCurrentState(instanceId={}, groupId={}): {}", csInstanceId, groupId, serviceGroup);
		final State temp = serviceGroupReadOnly.getMemberNested(groupId).getCurrentState();
		return temp;
	}

	public Map<String, Transition> getCurrentState() {
		Map<String, Transition> transitions = new HashMap<>();

		for (Group tempG : serviceGroupReadOnly.getAllMembersNested()) {
			transitions.put(tempG.getId(), new Transition(tempG.getId(), tempG.getType(), tempG.getPreviousState(),
					tempG.getCurrentState(), false));
		}
		return transitions;
	}

}
