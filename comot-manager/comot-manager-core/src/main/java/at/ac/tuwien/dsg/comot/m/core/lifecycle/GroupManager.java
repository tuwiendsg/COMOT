package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.events.AbstractEvent;
import at.ac.tuwien.dsg.comot.m.common.events.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.events.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.events.Transition;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotLifecycleException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceEntity;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

@Component
@Scope("prototype")
public class GroupManager {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	protected String csInstanceId;
	protected String serviceId;
	protected Group serviceGroup;
	protected Group serviceGroupReadOnly;
	protected Map<String, State> lastStates = new HashMap<>();
	protected AggregationStrategy strategy = new AggregationStrategy();

	public Group checkAndExecute(Action action, String groupId) throws ComotLifecycleException,
			IOException, ClassNotFoundException {

		Group group = serviceGroup.getMemberNested(groupId);

		if (group == null) {
			throw new ComotLifecycleException("The group '" + groupId + "' does not exist");
		}

		log.info(logId() + "checkAndExecute BEFORE groupId={} : {}", groupId, serviceGroup);

		if (!group.canExecute(action)) {
			throw new ComotLifecycleException("Action '" + action + "' is not allowed in state '"
					+ group.getCurrentState() + "'. Group " + groupId);
		}

		group.executeAction(action, strategy);
		serviceGroupReadOnly = (Group) Utils.deepCopy(serviceGroup);

		log.info(logId() + "checkAndExecute AFTER groupId={} : {}", groupId, serviceGroup);

		return group;
	}

	public void check(Action action, String groupId) throws ComotLifecycleException {

		Group group = serviceGroup.getMemberNested(groupId);

		if (!group.canExecute(action)) {
			throw new ComotLifecycleException("Action '" + action + "' is not allowed in state '"
					+ group.getCurrentState() + "'. Group " + groupId);
		}
	}

	public Group createGroupService(Action action, CloudService service) throws ComotLifecycleException,
			IOException, ClassNotFoundException {

		if (serviceGroup != null) {
			throw new ComotLifecycleException("Action '" + action
					+ "' is not allowed to be targeted at an existing CloudServiceInstance");
		}

		serviceGroup = new Group(service, State.INIT);

		for (Group group : serviceGroup.getAllMembersNested()) {
			lastStates.put(group.getId(), State.INIT);
		}

		checkAndExecute(action, serviceGroup.getId());
		log.info(serviceGroup.toString());

		return serviceGroup;
	}

	public Group createGroupUnitTopo(Action action, String parentId, ServiceEntity entity)
			throws ComotLifecycleException, ClassNotFoundException, IOException {

		String groupId = entity.getId();

		if (containsGroup(groupId)) {
			throw new ComotLifecycleException("The same group '" + groupId + "' can not be created twice");
		}

		if (entity instanceof ServiceUnit) {
			getGroup(parentId).addGroup(new Group((ServiceUnit) entity, State.INIT));
		} else if (entity instanceof ServiceTopology) {
			getGroup(parentId).addGroup(new Group((ServiceTopology) entity, State.INIT));
		} else {
			throw new IllegalArgumentException(entity.getClass().getName());
		}

		try {
			Group result;

			result = checkAndExecute(action, groupId);
			lastStates.put(groupId, State.INIT);

			return result;

		} catch (ComotLifecycleException e) {
			serviceGroup.removeMemberNested(groupId);
			throw e;
		}
	}

	public Group createGroupInstance(Action action, String parentId, UnitInstance instance)
			throws ClassNotFoundException, IOException, ComotException, ComotLifecycleException {

		String groupId = instance.getId();

		if (!containsGroup(parentId)) {
			throw new ComotLifecycleException("No such group: '" + parentId + "'");
		}

		getGroup(parentId).addGroup(new Group(groupId, Type.INSTANCE, State.INIT));

		try {
			Group result;

			result = checkAndExecute(action, groupId);
			lastStates.put(groupId, State.INIT);

			return result;

		} catch (ComotLifecycleException e) {
			serviceGroup.removeMemberNested(groupId);
			throw e;
		}
	}

	// protected void removeGroup(Action action, String groupId) throws ComotLifecycleException, ClassNotFoundException,
	// IOException {
	//
	// Group result = checkAndExecute(action, groupId);
	//
	// if(serviceId.equals(groupId)){
	// serviceGroup = null;
	// return;
	// }
	//
	// serviceGroup.removeMemberNested(groupId);
	//
	// for (Group member : result.getAllMembersNested()) {
	// lastStates.remove(member.getId());
	// }
	// }

	// protected List<String> removeAllGroupsInstances(Action action, String groupId) throws ComotLifecycleException,
	// ClassNotFoundException, IOException {
	//
	// Group result = checkAndExecute(action, groupId);
	//
	// List<String> list = new ArrayList<>();
	//
	// for (Group member : result.getAllMembersNested()) {
	// if (member.getType() == Type.INSTANCE) {
	// member.getParent().getMembers().remove(member);
	// list.add(member.getId());
	// lastStates.remove(member.getId());
	// }
	// }
	// return list;
	// }

	public Map<String, Transition> extractTransitions(AbstractEvent event) throws ClassNotFoundException,
			IOException {

		Map<String, Transition> transitions = new HashMap<>();

		if (event instanceof LifeCycleEvent) {

			Map<String, State> tempStates = new HashMap<>();
			boolean fresh;

			for (Group tempG : serviceGroup.getAllMembersNested()) {

				if (tempG.getCurrentState() != State.FINAL) {
					tempStates.put(tempG.getId(), tempG.getCurrentState());
				}

				if (tempG.getCurrentState().equals(lastStates.get(tempG.getId()))) {
					fresh = false;
				} else {
					fresh = true;
				}
				transitions.put(tempG.getId(), new Transition(tempG.getId(), tempG.getType(), tempG.getPreviousState(),
						tempG.getCurrentState(), fresh));
			}
			lastStates = tempStates;

		} else if (event instanceof CustomEvent) {
			for (Group tempG : serviceGroup.getAllMembersNested()) {
				transitions.put(tempG.getId(), new Transition(
						tempG.getId(), tempG.getType(), tempG.getPreviousState(), tempG.getCurrentState(), false));
			}
		}

		return transitions;
	}

	public boolean containsGroup(String groupId) {
		return serviceGroup.getMemberNested(groupId) != null;
	}

	public Group getGroup(String groupId) {
		return serviceGroup.getMemberNested(groupId);
	}

	public State getCurrentState(String groupId) {

		if (serviceGroupReadOnly == null || serviceGroupReadOnly.getMemberNested(groupId) == null) {
			return null;
		}
		final State temp = serviceGroupReadOnly.getMemberNested(groupId).getCurrentState();

		log.debug("getCurrentState(instanceId={}, groupId={}): {}", csInstanceId, groupId, temp);

		return temp;
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

	public String logId() {
		return "[ MANAGER_" + csInstanceId + "] ";
	}

	public void setCsInstanceId(String csInstanceId) {
		this.csInstanceId = csInstanceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

}
