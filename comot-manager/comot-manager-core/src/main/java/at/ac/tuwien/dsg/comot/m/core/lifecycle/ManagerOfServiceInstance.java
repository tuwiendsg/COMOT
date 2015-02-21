package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Action;
import at.ac.tuwien.dsg.comot.m.common.State;
import at.ac.tuwien.dsg.comot.m.common.StateMessage;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

@Component("prototype")
public class ManagerOfServiceInstance {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected InformationServiceMock infoService;

	protected String csInstanceId;
	protected Group serviceGroup;
	protected Map<String, Group> groups = new HashMap<>();
	protected Map<String, State> lastStates = new HashMap<>();

	public StateMessage createNewInstance(String serviceId, String csInstanceId) throws JAXBException, IOException {

		this.csInstanceId = csInstanceId;

		CloudService service = infoService.getServiceInformation(csInstanceId);

		serviceGroup = new Group(service, new AggregationStrategy());

		for (Group group : serviceGroup.getAllMembersNested()) {
			groups.put(group.getId(), group);
			lastStates.put(group.getId(), State.NONE);
		}

		log.info(" {}", serviceGroup);

		StateMessage message = executeAction(serviceId, Action.CREATE_NEW_INSTANCE);

		return message;
	}

	public StateMessage executeAction(String groupId, Action action) {

		Group group = groups.get(groupId);

		if (!group.canExecute(action)) {
			throw new ComotIllegalArgumentException("Action '" + action + "' is not allowed in state '"
					+ group.getCurrentState() + "'");
		}

		group.executeAction(action);

		Map<String, State> tempStates = new HashMap<>();
		StateMessage message = new StateMessage(groupId, action);

		for (String key : groups.keySet()) {

			tempStates.put(key, groups.get(key).getCurrentState());

			if (groups.get(key).getCurrentState().equals(lastStates.get(key))) {
				continue;
			}

			message.addOne(key, lastStates.get(key), groups.get(key).getCurrentState());
		}

		lastStates = tempStates;

		return message;
	}

	public Group getServiceGroup() {
		return serviceGroup;
	}

}
