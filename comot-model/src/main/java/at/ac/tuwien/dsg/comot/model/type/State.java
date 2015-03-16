package at.ac.tuwien.dsg.comot.model.type;

public enum State {

	INIT,
	PASSIVE,
	STARTING,

	// /**
	// * an instance is waiting for other condition before deployment. E.g, VM is creating by cloud, software is waiting
	// * for VM or waiting for other "connect to"
	// */
	// ALLOCATING ,
	//
	// /**
	// * the deployment command is assigned and waiting pioneer to get that command.
	// */
	// STAGING ,
	//
	// /**
	// * VM is initiating, e.g. setting up pioneer, install predefined packages; or software artifact is downloading,
	// * creating workspace, etc.
	// */
	// CONFIGURING ,
	//
	// /**
	// * running configuration script
	// */
	// INSTALLING ,

	DEPLOYING,
	RUNNING,
	ELASTIC_CHANGE,
	UPDATE,
	STOPPING,
	UNDEPLOYING,
	FINAL,

	/**
	 * the same with STAGING, but for custom configuration action at runtime
	 */
	// TODO
	STAGING_ACTION,
	ERROR;

	static public boolean isMember(String aName) {
		State[] states = State.values();
		for (State state : states) {
			if (state.name().equals(aName)) {
				return true;
			}
		}
		return false;
	}

}
