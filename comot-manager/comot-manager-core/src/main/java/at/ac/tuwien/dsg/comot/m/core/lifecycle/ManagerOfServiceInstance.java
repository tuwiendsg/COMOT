package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.EventMessage;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Transition;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component("prototype")
public class ManagerOfServiceInstance {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected String csInstanceId;
	protected String serviceId;
	protected Group serviceGroup;
	protected Map<String, Group> groups = new HashMap<>();
	protected Map<String, State> lastStates = new HashMap<>();
	protected AggregationStrategy strategy = new AggregationStrategy();

	public Map<String, Transition> executeAction(EventMessage event) throws JAXBException, IOException {

		String groupId = event.getGroupId();
		Action action = event.getAction();

		if (Action.NEW_INSTANCE_REQUESTED.equals(action)) {

			CloudService service = (CloudService) Utils.toObject(event.getMessage(), CloudService.class);

			this.csInstanceId = event.getCsInstanceId();
			this.serviceId = service.getId();

			serviceGroup = new Group(service, strategy);

			for (Group group : serviceGroup.getAllMembersNested()) {
				groups.put(group.getId(), group);
				lastStates.put(group.getId(), State.NONE);
			}

		} else if (Action.DEPLOYMENT_REQUESTED.equals(action) && !groups.containsKey(groupId)) {

			CloudService service = (CloudService) Utils.toObject(event.getMessage(), CloudService.class);
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
		Map<String, State> tempStates = new HashMap<>();
		Map<String, Transition> transitions = new HashMap<>();

		log.info("group check: {} {}", groupId, group);

		if (!group.canExecute(action)) {
			throw new ComotIllegalArgumentException("Action '" + action + "' is not allowed in state '"
					+ group.getCurrentState() + "'");
		}

		group.executeAction(action);

		for (String key : groups.keySet()) {

			tempStates.put(key, groups.get(key).getCurrentState());
			if (groups.get(key).getCurrentState().equals(lastStates.get(key))) {
				continue;
			}
			transitions.put(key, new Transition(lastStates.get(key), groups.get(key).getCurrentState()));
		}

		lastStates = tempStates;

		log.info("transitions {}", transitions);

		return transitions;
	}

	public Group getServiceGroup() {
		return serviceGroup;
	}

	public Map<String, Group> getGroups() {
		return groups;
	}

	public String getServiceId() {
		return serviceId;
	}

}
