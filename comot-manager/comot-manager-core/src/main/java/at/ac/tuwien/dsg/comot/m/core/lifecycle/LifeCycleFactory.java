package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

public class LifeCycleFactory {

	protected static LifeCycle unitInstanceLc;
	protected static LifeCycle serviceLc;
	protected static LifeCycle othersLc;

	static {

		// SERVICE
		serviceLc = new LifeCycle();

		serviceLc.addTransition(State.INIT, Action.CREATED, State.PASSIVE);

		serviceLc.addTransition(State.PASSIVE, Action.STARTED, State.STARTING);
		serviceLc.addTransition(State.PASSIVE, Action.REMOVED, State.FINAL);

		serviceLc.addTransition(State.STARTING, Action.DEPLOYMENT_STARTED, State.DEPLOYING);

		serviceLc.addTransition(State.DEPLOYING, Action.DEPLOYED, State.RUNNING);

		serviceLc.addTransition(State.RUNNING, Action.ELASTIC_CHANGE_STARTED, State.ELASTIC_CHANGE);
		serviceLc.addTransition(State.RUNNING, Action.UPDATE_STARTED, State.UPDATE);
		serviceLc.addTransition(State.RUNNING, Action.STOPPED, State.STOPPING);

		serviceLc.addTransition(State.ELASTIC_CHANGE, Action.UPDATE_FINISHED, State.RUNNING);

		serviceLc.addTransition(State.UPDATE, Action.UPDATE_FINISHED, State.RUNNING);

		serviceLc.addTransition(State.STOPPING, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);

		serviceLc.addTransition(State.UNDEPLOYING, Action.UNDEPLOYED, State.PASSIVE);

		// UNIT , TOPOLOGY
		othersLc = new LifeCycle();

		othersLc.addTransition(State.INIT, Action.CREATED, State.PASSIVE);
		othersLc.addTransition(State.INIT, Action.UNDEPLOYMENT_STARTED, State.UPDATE, State.UPDATE);

		othersLc.addTransition(State.PASSIVE, Action.STARTED, State.STARTING);
		othersLc.addTransition(State.PASSIVE, Action.REMOVED, State.FINAL);

		othersLc.addTransition(State.STARTING, Action.DEPLOYMENT_STARTED, State.DEPLOYING);

		othersLc.addTransition(State.DEPLOYING, Action.DEPLOYED, State.RUNNING);

		othersLc.addTransition(State.RUNNING, Action.ELASTIC_CHANGE_STARTED, State.ELASTIC_CHANGE);
		othersLc.addTransition(State.RUNNING, Action.UPDATE_STARTED, State.UPDATE);
		othersLc.addTransition(State.RUNNING, Action.STOPPED, State.STOPPING);

		othersLc.addTransition(State.ELASTIC_CHANGE, Action.UPDATE_FINISHED, State.RUNNING);

		othersLc.addTransition(State.UPDATE, Action.UPDATE_FINISHED, State.RUNNING);
		othersLc.addTransition(State.UPDATE, Action.REMOVED, State.FINAL);

		othersLc.addTransition(State.STOPPING, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);

		othersLc.addTransition(State.UNDEPLOYING, Action.UNDEPLOYED, State.PASSIVE);

		// UNIT INSTANCE
		unitInstanceLc = new LifeCycle();

		unitInstanceLc.addTransition(State.INIT, Action.DEPLOYMENT_STARTED, State.DEPLOYING);

		unitInstanceLc.addTransition(State.DEPLOYING, Action.DEPLOYED, State.RUNNING, State.DEPLOYING);
		unitInstanceLc.addTransition(State.DEPLOYING, Action.DEPLOYED, State.RUNNING, State.ELASTIC_CHANGE);
		unitInstanceLc.addTransition(State.DEPLOYING, Action.DEPLOYED, State.RUNNING, State.UPDATE);

		unitInstanceLc.addTransition(State.RUNNING, Action.ELASTIC_CHANGE_STARTED, State.ELASTIC_CHANGE);
		unitInstanceLc.addTransition(State.RUNNING, Action.UPDATE_STARTED, State.UPDATE);
		unitInstanceLc.addTransition(State.RUNNING, Action.STOPPED, State.STOPPING);

		unitInstanceLc.addTransition(State.ELASTIC_CHANGE, Action.UPDATE_FINISHED, State.RUNNING);
		unitInstanceLc.addTransition(State.ELASTIC_CHANGE, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);

		unitInstanceLc.addTransition(State.UPDATE, Action.UPDATE_FINISHED, State.RUNNING);
		unitInstanceLc.addTransition(State.UPDATE, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);

		unitInstanceLc.addTransition(State.STOPPING, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);

		unitInstanceLc.addTransition(State.UNDEPLOYING, Action.UNDEPLOYED, State.FINAL);

	}

	public static LifeCycle getLifeCycle(Type type) {

		switch (type) {
		case INSTANCE:
			return unitInstanceLc;
		case SERVICE:
			return serviceLc;
		case TOPOLOGY:
			return othersLc;
		case UNIT:
			return othersLc;
		default:
			throw new IllegalArgumentException("Unsupported type '" + type + "'");
		}

	}

}
