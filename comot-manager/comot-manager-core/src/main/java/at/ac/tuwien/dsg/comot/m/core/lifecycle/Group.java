package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

public class Group {

	protected String id;
	protected State currentState;
	protected State previousState;
	protected Type type;
	protected Group parent;
	protected List<Group> members = new ArrayList<Group>();
	protected AggregationStrategy strategy;

	public Group(CloudService service, AggregationStrategy strategy) {
		this(service.getId(), Type.SERVICE, State.NONE, null, strategy);

		for (ServiceTopology topo : service.getServiceTopologies()) {
			addTopology(topo);
		}
	}

	public Group(ServiceTopology topology, Group parent, AggregationStrategy strategy) {
		this(topology.getId(), Type.TOPOLOGY, State.NONE, parent, strategy);

		for (ServiceUnit unit : topology.getServiceUnits()) {
			addUnit(unit);
		}

		for (ServiceTopology topo : topology.getServiceTopologies()) {
			addTopology(topo);
		}
	}

	public Group(ServiceUnit unit, Group parent, AggregationStrategy strategy) {
		this(unit.getId(), Type.UNIT, State.NONE, parent, strategy);

		for (UnitInstance instance : unit.getInstances()) {
			addInstance(instance);
		}
	}

	public Group(UnitInstance instance, Group parent, AggregationStrategy strategy) {
		this(instance.getId(), Type.INSTANCE, State.IDLE, parent, strategy);
	}

	public Group(String id, Type type, State state, Group parent, AggregationStrategy strategy) {
		super();
		this.id = id;
		this.currentState = state;
		this.type = type;
		this.parent = parent;
		this.strategy = strategy;
	}

	public Group addTopology(ServiceTopology topology) {
		Group temp = new Group(topology, this, strategy);
		members.add(temp);
		return temp;
	}

	public Group addUnit(ServiceUnit unit) {
		Group temp = new Group(unit, this, strategy);
		members.add(temp);
		return temp;
	}

	public Group addInstance(UnitInstance instance) {
		Group temp = new Group(instance, this, strategy);
		members.add(temp);
		return temp;
	}

	enum Type {
		SERVICE, TOPOLOGY, UNIT, INSTANCE
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

	public void executeAction(Action action) {

		State nextState = null;

		if (members.isEmpty()) {

			nextState = currentState.execute(action);

			if (nextState != null) {
				moveToState(nextState);
			}

			if (parent != null) {
				parent.refreshState();
			}

		} else {
			for (Group member : members) {
				member.executeAction(action);
			}
		}

	}

	public List<Group> getAllMembersNested() {

		List<Group> nested = new ArrayList<Group>();
		nested.add(this);

		for (Group member : members) {
			nested.addAll(member.getAllMembersNested());
		}

		return nested;

	}

	protected void refreshState() {

		State nextState = strategy.determineState(currentState, type, members);

		if (nextState.equals(currentState)) {
			return;
		}

		moveToState(nextState);

		if (parent != null) {
			parent.refreshState();
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

	public AggregationStrategy getStrategy() {
		return strategy;
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
