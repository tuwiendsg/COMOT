package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import java.util.List;

import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.model.type.State;

public class AggregationStrategy {

	public State determineState(State currentState, Type type,
			List<Group> members) {

		if (members.isEmpty()) {
			return currentState;
		} else {

//			for (Group member : members) {
//				if (member.getCurrentState() == State.ERROR) {
//					return State.ERROR;
//				}
//			}

			State one = members.get(0).getCurrentState();

			if (allMembersTheSameState(members)) {
				if (type == Type.UNIT && one == State.FINAL) {
					return State.PASSIVE;
				} else {
					return one;
				}
			} else {

				return currentState;
			}
		}

	}

	protected boolean allMembersTheSameState(List<Group> members) {

		State one = null;

		for (Group member : members) {
			if (one == null) {
				one = member.getCurrentState();
			} else if (!member.getCurrentState().equals(one)) {
				return false;
			}
		}
		return true;
	}
}
