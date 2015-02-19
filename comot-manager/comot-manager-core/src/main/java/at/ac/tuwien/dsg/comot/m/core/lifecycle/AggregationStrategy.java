package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.util.List;

import at.ac.tuwien.dsg.comot.m.common.State;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.Group.Type;

public class AggregationStrategy {

	public State determineState(State currentState, Type type,
			List<Group> members) {

		State one;
		if (members.isEmpty()) {
			return currentState;
		} else {
			one = members.get(0).getCurrentState();
		}

		for (Group member : members) {
			if (!member.getCurrentState().equals(one)) {
				return currentState;
			}
		}

		return one;

	}
}
