package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.model.type.State;

public class LifeCycleFactory {

	protected static LifeCycle unitInstanceLc;
	protected static LifeCycle serviceLc;
	protected static LifeCycle othersLc;

	static {

		// SERVICE
		serviceLc = new LifeCycle();

		serviceLc.addTransition(State.INIT, Action.CREATED, State.PASSIVE);

		serviceLc.addTransition(State.PASSIVE, Action.START, State.PASSIVE);
		serviceLc.addTransition(State.PASSIVE, Action.REMOVED, State.FINAL);
		serviceLc.addTransition(State.PASSIVE, Action.DEPLOYMENT_STARTED, State.DEPLOYING);

		serviceLc.addTransition(State.DEPLOYING, Action.DEPLOYED, State.RUNNING);

		serviceLc.addTransition(State.RUNNING, Action.ELASTIC_CHANGE_STARTED, State.ELASTIC_CHANGE);
		serviceLc.addTransition(State.RUNNING, Action.MAINTENANCE_STARTED, State.MAINTENANCE);
		serviceLc.addTransition(State.RUNNING, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);
		serviceLc.addTransition(State.RUNNING, Action.STOP, State.RUNNING);
		serviceLc.addTransition(State.RUNNING, Action.START_TEST, State.RUNNING);
		serviceLc.addTransition(State.RUNNING, Action.FINISH_TEST, State.RUNNING);
		serviceLc.addTransition(State.RUNNING, Action.START_MAINTENANCE, State.RUNNING);
		serviceLc.addTransition(State.RUNNING, Action.START_CONTROLLER, State.RUNNING);
		serviceLc.addTransition(State.RUNNING, Action.STOP_CONTROLLER, State.RUNNING);
		serviceLc.addTransition(State.RUNNING, Action.RECONFIGURE_ELASTICITY, State.RUNNING);

		serviceLc.addTransition(State.ELASTIC_CHANGE, Action.ELASTIC_CHANGE_FINISHED, State.RUNNING);

		serviceLc.addTransition(State.MAINTENANCE, Action.MAINTENANCE_FINISHED, State.RUNNING);

		serviceLc.addTransition(State.RUNNING, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);
		serviceLc.addTransition(State.ERROR, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);

		serviceLc.addTransition(State.UNDEPLOYING, Action.UNDEPLOYED, State.PASSIVE);

		// UNIT , TOPOLOGY
		othersLc = new LifeCycle();

		othersLc.addTransition(State.INIT, Action.CREATED, State.PASSIVE);
		othersLc.addTransition(State.INIT, Action.DEPLOYMENT_STARTED, State.MAINTENANCE);

		othersLc.addTransition(State.PASSIVE, Action.START, State.PASSIVE);
		othersLc.addTransition(State.PASSIVE, Action.REMOVED, State.FINAL);
		othersLc.addTransition(State.PASSIVE, Action.DEPLOYMENT_STARTED, State.DEPLOYING);

		othersLc.addTransition(State.DEPLOYING, Action.DEPLOYED, State.RUNNING);

		othersLc.addTransition(State.RUNNING, Action.ELASTIC_CHANGE_STARTED, State.ELASTIC_CHANGE);
		othersLc.addTransition(State.RUNNING, Action.MAINTENANCE_STARTED, State.MAINTENANCE);
		othersLc.addTransition(State.RUNNING, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);
		othersLc.addTransition(State.RUNNING, Action.STOP, State.RUNNING);
		othersLc.addTransition(State.RUNNING, Action.START_TEST, State.RUNNING);
		othersLc.addTransition(State.RUNNING, Action.FINISH_TEST, State.RUNNING);
		othersLc.addTransition(State.RUNNING, Action.START_MAINTENANCE, State.RUNNING);
		othersLc.addTransition(State.RUNNING, Action.START_CONTROLLER, State.RUNNING);
		othersLc.addTransition(State.RUNNING, Action.STOP_CONTROLLER, State.RUNNING);
		othersLc.addTransition(State.RUNNING, Action.RECONFIGURE_ELASTICITY, State.RUNNING);

		othersLc.addTransition(State.ELASTIC_CHANGE, Action.ELASTIC_CHANGE_FINISHED, State.RUNNING);

		othersLc.addTransition(State.MAINTENANCE, Action.MAINTENANCE_FINISHED, State.RUNNING);
		othersLc.addTransition(State.MAINTENANCE, Action.REMOVED, State.FINAL);

		othersLc.addTransition(State.RUNNING, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);
		othersLc.addTransition(State.ERROR, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);

		othersLc.addTransition(State.UNDEPLOYING, Action.UNDEPLOYED, State.PASSIVE);

		// UNIT INSTANCE
		unitInstanceLc = new LifeCycle();

		unitInstanceLc.addTransition(State.INIT, Action.DEPLOYMENT_STARTED, State.DEPLOYING);

		unitInstanceLc.addTransition(State.DEPLOYING, Action.DEPLOYED, State.RUNNING, State.DEPLOYING);
		unitInstanceLc.addTransition(State.DEPLOYING, Action.DEPLOYED, State.ELASTIC_CHANGE, State.ELASTIC_CHANGE);
		unitInstanceLc.addTransition(State.DEPLOYING, Action.DEPLOYED, State.MAINTENANCE, State.MAINTENANCE);

		unitInstanceLc.addTransition(State.RUNNING, Action.ELASTIC_CHANGE_STARTED, State.ELASTIC_CHANGE);
		unitInstanceLc.addTransition(State.RUNNING, Action.MAINTENANCE_STARTED, State.MAINTENANCE);
		unitInstanceLc.addTransition(State.RUNNING, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);
		unitInstanceLc.addTransition(State.RUNNING, Action.STOP, State.RUNNING);
		unitInstanceLc.addTransition(State.RUNNING, Action.START_TEST, State.RUNNING);
		unitInstanceLc.addTransition(State.RUNNING, Action.FINISH_TEST, State.RUNNING);
		unitInstanceLc.addTransition(State.RUNNING, Action.START_MAINTENANCE, State.RUNNING);
		unitInstanceLc.addTransition(State.RUNNING, Action.START_CONTROLLER, State.RUNNING);
		unitInstanceLc.addTransition(State.RUNNING, Action.STOP_CONTROLLER, State.RUNNING);
		unitInstanceLc.addTransition(State.RUNNING, Action.RECONFIGURE_ELASTICITY, State.RUNNING);

		unitInstanceLc.addTransition(State.ELASTIC_CHANGE, Action.ELASTIC_CHANGE_FINISHED, State.RUNNING);
		unitInstanceLc.addTransition(State.ELASTIC_CHANGE, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);

		unitInstanceLc.addTransition(State.MAINTENANCE, Action.MAINTENANCE_FINISHED, State.RUNNING);
		unitInstanceLc.addTransition(State.MAINTENANCE, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);

		unitInstanceLc.addTransition(State.RUNNING, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);
		unitInstanceLc.addTransition(State.ERROR, Action.UNDEPLOYMENT_STARTED, State.UNDEPLOYING);

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
