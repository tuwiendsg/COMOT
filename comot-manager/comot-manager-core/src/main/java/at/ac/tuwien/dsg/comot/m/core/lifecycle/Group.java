package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.State;

public class Group implements Serializable {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private static final long serialVersionUID = 2534198672357223629L;

	protected String id;
	protected State currentState;
	protected State previousState;
	protected Type type;
	protected Group parent;
	protected List<Group> members = new ArrayList<Group>();

	public Group(CloudService service, State state) {
		this(service.getId(), Type.SERVICE, state);

		for (ServiceTopology topo : service.getServiceTopologies()) {
			Group temp = new Group(topo, state);
			addGroup(temp);
		}
	}

	public Group(ServiceTopology topology, State state) {
		this(topology.getId(), Type.TOPOLOGY, state);

		for (ServiceUnit unit : topology.getServiceUnits()) {
			Group temp = new Group(unit, state);
			addGroup(temp);
		}

		for (ServiceTopology topo : topology.getServiceTopologies()) {
			Group temp = new Group(topo, state);
			addGroup(temp);
		}
	}

	public Group(ServiceUnit unit, State state) {
		this(unit.getId(), Type.UNIT, state);

		for (UnitInstance instance : unit.getInstances()) {
			Group temp = new Group(instance.getId(), Type.INSTANCE, state);
			addGroup(temp);
		}
	}

	public Group(String id, Type type, State state) {
		super();
		this.id = id;
		this.currentState = state;
		this.type = type;
	}

	public Group addGroup(String id, Type type, State state) {
		Group temp = new Group(id, type, state);
		members.add(temp);
		return temp;
	}

	public void addGroup(Group group) {
		group.parent = this;
		members.add(group);
	}

	public boolean canExecute(Action action) {

		if (members.isEmpty()) {
			if (LifeCycleFactory.getLifeCycle(type).executeAction(parent.getCurrentState(), currentState, action) == null) {
				log.warn(
						"Action '{}' is not allowed in state '{}' patentState '{}'. GroupId={} , type={} <- the root of problem",
						action, currentState, parent.getCurrentState(), id, type);
				return false;
			} else {
				return true;
			}

		} else {
			for (Group member : members) {
				if (!member.canExecute(action)) {
					return false;
				}
			}
			return true;
		}
	}

	public String notAllowedExecutionReason(Action action) {

		String reason = null;

		if (members.isEmpty()) {
			if (LifeCycleFactory.getLifeCycle(type).executeAction(parent.getCurrentState(), currentState, action) == null) {
				return "ROOT: Action '" + action + "' is not allowed in state '" + currentState + "' patentState '"
						+ parent.getCurrentState() + "'. GroupId=" + id + " , type=" + type;
			} else {
				return null;
			}

		} else {
			for (Group member : members) {
				if (null != (reason = member.notAllowedExecutionReason(action))) {
					return reason;
				}
			}
			return null;
		}

	}

	public void executeAction(Action action, AggregationStrategy strategy) {

		State nextState = null;

		if (members.isEmpty()) {

			nextState = LifeCycleFactory.getLifeCycle(type)
					.executeAction(parent.getCurrentState(), currentState, action);

			if (nextState != null) {
				moveToState(nextState);
			}

			if (parent != null) {
				parent.refreshState(strategy);
			}

		} else {
			for (Group member : members) {
				member.executeAction(action, strategy);
			}
		}

	}

	public Group getMemberNested(String gouprId) {

		Group temp = null;

		if (this.getId().equals(gouprId)) {
			return this;
		} else {
			for (Group member : members) {
				temp = member.getMemberNested(gouprId);
				if (temp != null) {
					return temp;
				}
			}
		}
		return null;

	}

	public List<Group> getAllMembersNested() {

		List<Group> nested = new ArrayList<Group>();
		nested.add(this);

		for (Group member : members) {
			nested.addAll(member.getAllMembersNested());
		}

		return nested;

	}

	protected void refreshState(AggregationStrategy strategy) {

		State nextState = strategy.determineState(currentState, type, members);

		if (nextState.equals(currentState)) {
			return;
		}

		moveToState(nextState);

		if (parent != null) {
			parent.refreshState(strategy);
		}

	}

	protected void moveToState(State nextState) {
		previousState = currentState;
		currentState = nextState;
	}

	public boolean removeMemberNested(String id) {

		if (this.id.equals(id)) {
			this.parent.getMembers().remove(this);
			return true;
		} else {
			for (Group member : members) {
				if (member.removeMemberNested(id)) {
					return true;
				}
			}
			return false;
		}
	}

	// GENERATED

	// @Override
	// public String toString() {
	// return "{ \"id\" : \"" + id + "\" , \"currentState\" : \""
	// + currentState + "\" , \"previousState\" : \"" + previousState
	// + "\" , \"type\" : \"" + type + "\" , \"parent\" : \""
	// + ((parent == null) ? null : parent.getId())
	// + "\" , \"members\" : " + members + "}";
	// }

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return "( " + type + ":" + id + ", " + previousState + " -> " + currentState + ", members=" + members + ")";
	}

	public Group getParent() {
		return parent;
	}

	public Type getType() {
		return type;
	}

	public List<Group> getMembers() {
		return members;
	}

	public State getCurrentState() {
		return currentState;
	}

	public State getPreviousState() {
		return previousState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Group other = (Group) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
