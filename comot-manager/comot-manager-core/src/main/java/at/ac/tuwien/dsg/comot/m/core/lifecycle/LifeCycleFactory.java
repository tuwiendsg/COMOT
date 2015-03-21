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

		serviceLc.addTransition(State.PASSIVE, Action.STARTED, State.PASSIVE);
		serviceLc.addTransition(State.PASSIVE, Action.REMOVED, State.FINAL);
		serviceLc.addTransition(State.PASSIVE, Action.DEPLOYMENT_STARTED, State.DEPLOYING);

		serviceLc.addTransition(State.DEPLOYING, Action.DEPLOYED, State.RUNNING);

		serviceLc.addTransition(State.RUNNING, Action.ELASTIC_CHANGE_STARTED, State.ELASTIC_CHANGE);
		serviceLc.addTransition(State.RUNNING, Action.UPDATE_STARTED, State.UPDATE);
		serviceLc.addTransition(State.RUNNING, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);
		serviceLc.addTransition(State.RUNNING, Action.STOPPED, State.RUNNING);
		serviceLc.addTransition(State.RUNNING, Action.TEST_STARTED, State.RUNNING);
		serviceLc.addTransition(State.RUNNING, Action.TEST_FINISHED, State.RUNNING);
		serviceLc.addTransition(State.RUNNING, Action.UPDATE, State.RUNNING);
		serviceLc.addTransition(State.RUNNING, Action.CONTROLLER_STARTED, State.RUNNING);
		serviceLc.addTransition(State.RUNNING, Action.CONTROLLER_STOPPED, State.RUNNING);
		serviceLc.addTransition(State.RUNNING, Action.RECONFIGURED_ELASTICITY, State.RUNNING);

		serviceLc.addTransition(State.ELASTIC_CHANGE, Action.ELASTIC_CHANGE_FINISHED, State.RUNNING);

		serviceLc.addTransition(State.UPDATE, Action.UPDATE_FINISHED, State.RUNNING);

		serviceLc.addTransition(State.RUNNING, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);

		serviceLc.addTransition(State.UNDEPLOYING, Action.UNDEPLOYED, State.PASSIVE);

		// UNIT , TOPOLOGY
		othersLc = new LifeCycle();

		othersLc.addTransition(State.INIT, Action.CREATED, State.PASSIVE);
		othersLc.addTransition(State.INIT, Action.DEPLOYMENT_STARTED, State.UPDATE);

		othersLc.addTransition(State.PASSIVE, Action.STARTED, State.PASSIVE);
		othersLc.addTransition(State.PASSIVE, Action.REMOVED, State.FINAL);
		othersLc.addTransition(State.PASSIVE, Action.DEPLOYMENT_STARTED, State.DEPLOYING);

		othersLc.addTransition(State.DEPLOYING, Action.DEPLOYED, State.RUNNING);

		othersLc.addTransition(State.RUNNING, Action.ELASTIC_CHANGE_STARTED, State.ELASTIC_CHANGE);
		othersLc.addTransition(State.RUNNING, Action.UPDATE_STARTED, State.UPDATE);
		othersLc.addTransition(State.RUNNING, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);
		othersLc.addTransition(State.RUNNING, Action.STOPPED, State.RUNNING);
		othersLc.addTransition(State.RUNNING, Action.TEST_STARTED, State.RUNNING);
		othersLc.addTransition(State.RUNNING, Action.TEST_FINISHED, State.RUNNING);
		othersLc.addTransition(State.RUNNING, Action.UPDATE, State.RUNNING);
		othersLc.addTransition(State.RUNNING, Action.CONTROLLER_STARTED, State.RUNNING);
		othersLc.addTransition(State.RUNNING, Action.CONTROLLER_STOPPED, State.RUNNING);
		othersLc.addTransition(State.RUNNING, Action.RECONFIGURED_ELASTICITY, State.RUNNING);

		othersLc.addTransition(State.ELASTIC_CHANGE, Action.ELASTIC_CHANGE_FINISHED, State.RUNNING);

		othersLc.addTransition(State.UPDATE, Action.UPDATE_FINISHED, State.RUNNING);
		othersLc.addTransition(State.UPDATE, Action.REMOVED, State.FINAL);

		othersLc.addTransition(State.RUNNING, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);

		othersLc.addTransition(State.UNDEPLOYING, Action.UNDEPLOYED, State.PASSIVE);

		// UNIT INSTANCE
		unitInstanceLc = new LifeCycle();

		unitInstanceLc.addTransition(State.INIT, Action.DEPLOYMENT_STARTED, State.DEPLOYING);

		unitInstanceLc.addTransition(State.DEPLOYING, Action.DEPLOYED, State.RUNNING);
		unitInstanceLc.addTransition(State.DEPLOYING, Action.DEPLOYED, State.ELASTIC_CHANGE, State.ELASTIC_CHANGE);
		unitInstanceLc.addTransition(State.DEPLOYING, Action.DEPLOYED, State.UPDATE, State.UPDATE);

		unitInstanceLc.addTransition(State.RUNNING, Action.ELASTIC_CHANGE_STARTED, State.ELASTIC_CHANGE);
		unitInstanceLc.addTransition(State.RUNNING, Action.UPDATE_STARTED, State.UPDATE);
		unitInstanceLc.addTransition(State.RUNNING, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);
		unitInstanceLc.addTransition(State.RUNNING, Action.STOPPED, State.RUNNING);
		unitInstanceLc.addTransition(State.RUNNING, Action.TEST_STARTED, State.RUNNING);
		unitInstanceLc.addTransition(State.RUNNING, Action.TEST_FINISHED, State.RUNNING);
		unitInstanceLc.addTransition(State.RUNNING, Action.UPDATE, State.RUNNING);
		unitInstanceLc.addTransition(State.RUNNING, Action.CONTROLLER_STARTED, State.RUNNING);
		unitInstanceLc.addTransition(State.RUNNING, Action.CONTROLLER_STOPPED, State.RUNNING);
		unitInstanceLc.addTransition(State.RUNNING, Action.RECONFIGURED_ELASTICITY, State.RUNNING);

		unitInstanceLc.addTransition(State.ELASTIC_CHANGE, Action.ELASTIC_CHANGE_FINISHED, State.RUNNING);
		unitInstanceLc.addTransition(State.ELASTIC_CHANGE, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);

		unitInstanceLc.addTransition(State.UPDATE, Action.UPDATE_FINISHED, State.RUNNING);
		unitInstanceLc.addTransition(State.UPDATE, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);

		unitInstanceLc.addTransition(State.RUNNING, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);

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
