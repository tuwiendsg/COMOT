package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.util.ArrayList;

import at.ac.tuwien.dsg.comot.m.common.Action;
import at.ac.tuwien.dsg.comot.m.common.State;

public class Instance extends Group {

	public Instance(String id, Group parent, AggregationStrategy strategy) {
		super(id, Type.INSTANCE, parent, strategy);
	}

	@Override
	public boolean canExecute(Action action) {
		return (currentState.execute(action) == null) ? false : true;
	}

	@Override
	public State executeAction(Action action) {
		State nextState = currentState.execute(action);

		if (nextState != null) {
			moveState(nextState);
		}

		return nextState;
	}

	@Override
	public void refreshState() {
	}

}
