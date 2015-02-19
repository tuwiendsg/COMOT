package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;

public class Group {

	protected String id;
	protected State currentState;
	protected State previousState;
	protected Type type;
	protected Group parent;
	protected List<Group> members = new ArrayList<Group>();
	protected AggregationStrategy strategy;

	public Group(CloudService service, AggregationStrategy strategy) {
		this(service.getId(), Type.SERVICE, null, strategy);

		for (ServiceTopology topo : service.getServiceTopologies()) {
			addTopology(topo);
		}
	}

	public Group(ServiceTopology topology, Group parent,
			AggregationStrategy strategy) {
		this(topology.getId(), Type.TOPOLOGY, parent, strategy);

		for (ServiceUnit unit : topology.getServiceUnits()) {
			addUnit(unit);
		}

		for (ServiceTopology topo : topology.getServiceTopologies()) {
			addTopology(topo);
		}
	}

	public Group(ServiceUnit unit, Group parent, AggregationStrategy strategy) {
		this(unit.getId(), Type.UNIT, parent, strategy);

		for (UnitInstance instance : unit.getInstances()) {
			addInstance(instance);
		}
	}

	public Group(String id, Type type, Group parent,
			AggregationStrategy strategy) {
		super();
		this.id = id;
		this.currentState = State.IDLE;
		this.type = type;
		this.parent = parent;
		this.strategy = strategy;
	}

	public void addTopology(ServiceTopology topology) {
		members.add(new Group(topology, this, strategy));
	}

	public void addUnit(ServiceUnit unit) {
		members.add(new Group(unit, this, strategy));
	}

	public void addInstance(UnitInstance instance) {
		members.add(new Instance(instance.getId(), this, strategy));
	}

	enum Type {
		SERVICE, TOPOLOGY, UNIT, INSTANCE
	}

	public boolean canExecute(Action action) {

		for (Group menber : members) {
			if (!menber.canExecute(action)) {
				return false;
			}
		}
		return true;
	}

	public State executeAction(Action action) {

		State nextState = null;

		for (Group member : members) {
			nextState = member.executeAction(action);
		}

		refreshState();

		return nextState;
	}

	public void refreshState() {
		moveState(strategy.determineState(currentState, type, members));
	}

	protected void moveState(State newState) {
		previousState = currentState;
		currentState = newState;
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

}