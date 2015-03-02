package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

public class Group implements Serializable {

	private static final long serialVersionUID = 2534198672357223629L;

	protected String id;
	protected State currentState;
	protected State previousState;
	protected Type type;
	protected Group parent;
	protected List<Group> members = new ArrayList<Group>();

	public Group(CloudService service, State state) {
		this(service.getId(), Type.SERVICE, state, null);

		for (ServiceTopology topo : service.getServiceTopologies()) {
			addTopology(topo, state);
		}
	}

	public Group(ServiceTopology topology, State state, Group parent) {
		this(topology.getId(), Type.TOPOLOGY, state, parent);

		for (ServiceUnit unit : topology.getServiceUnits()) {
			addUnit(unit, state);
		}

		for (ServiceTopology topo : topology.getServiceTopologies()) {
			addTopology(topo, state);
		}
	}

	public Group(ServiceUnit unit, State state, Group parent) {
		this(unit.getId(), Type.UNIT, state, parent);

		for (UnitInstance instance : unit.getInstances()) {
			addInstance(instance, state);
		}
	}

	public Group(UnitInstance instance, State state, Group parent) {
		this(instance.getId(), Type.INSTANCE, state, parent);
	}

	public Group(String id, Type type, State state, Group parent) {
		super();
		this.id = id;
		this.currentState = state;
		this.type = type;
		this.parent = parent;
	}

	public Group addTopology(ServiceTopology topology, State state) {
		Group temp = new Group(topology, state, this);
		members.add(temp);
		return temp;
	}

	public Group addUnit(ServiceUnit unit, State state) {
		Group temp = new Group(unit, state, this);
		members.add(temp);
		return temp;
	}

	public Group addInstance(UnitInstance instance, State state) {
		Group temp = new Group(instance, state, this);
		members.add(temp);
		return temp;
	}

	public boolean canExecute(Action action) {

		if (members.isEmpty()) {
			return (currentState.execute(action) == null) ? false : true;

		} else {
			for (Group menber : members) {
				if (!menber.canExecute(action)) {
					return false;
				}
			}
			return true;
		}
	}

	public void executeAction(Action action, AggregationStrategy strategy) {

		State nextState = null;

		if (members.isEmpty()) {

			nextState = currentState.execute(action);

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

	// GENERATED

	@Override
	public String toString() {
		return "{ \"id\" : \"" + id + "\" , \"currentState\" : \""
				+ currentState + "\" , \"previousState\" : \"" + previousState
				+ "\" , \"type\" : \"" + type + "\" , \"parent\" : \""
				+ ((parent == null) ? null : parent.getId())
				+ "\" , \"members\" : " + members + "}";
	}

	public String getId() {
		return id;
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
