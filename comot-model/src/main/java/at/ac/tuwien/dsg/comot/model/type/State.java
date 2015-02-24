package at.ac.tuwien.dsg.comot.model.type;

public enum State {

	NONE {
		@Override
		public State execute(Action action) {
			switch (action) {
			case NEW_INSTANCE_REQUESTED:
				return PREPARATION;
			default:
				return null;
			}
		}
	},

	PREPARATION {
		@Override
		public State execute(Action action) {
			switch (action) {
			case PREPARED:
				return IDLE;
			default:
				return null;
			}
		}
	},

	IDLE {
		@Override
		public State execute(Action action) {
			switch (action) {
			case DEPLOYMENT_REQUESTED:
				return DEPLOYMENT_ALLOCATING;
			case INSTANCE_REMOVAL_REQUESTED:
				return NONE;
			default:
				return null;
			}
		}
	},

	/**
	 * an instance is waiting for other condition before deployment. E.g, VM is creating by cloud, software is waiting
	 * for VM or waiting for other "connect to"
	 */
	DEPLOYMENT_ALLOCATING {
		@Override
		public State execute(Action action) {
			switch (action) {
			case ALLOCATED:
				return DEPLOYMENT_STAGING;
			case STAGED:
				return DEPLOYMENT_CONFIGURING;
			case CONFIGURED:
				return DEPLOYMENT_INSTALLING;
			case INSTALLED:
				return OPERATION_RUNNING;
			default:
				return null;
			}
		}
	},

	/**
	 * the deployment command is assigned and waiting pioneer to get that command.
	 */
	DEPLOYMENT_STAGING {
		@Override
		public State execute(Action action) {
			switch (action) {
			case STAGED:
				return DEPLOYMENT_CONFIGURING;
			case CONFIGURED:
				return DEPLOYMENT_INSTALLING;
			case INSTALLED:
				return OPERATION_RUNNING;
			default:
				return null;
			}
		}
	},

	/**
	 * VM is initiating, e.g. setting up pioneer, install predefined packages; or software artifact is downloading,
	 * creating workspace, etc.
	 */
	DEPLOYMENT_CONFIGURING {
		@Override
		public State execute(Action action) {
			switch (action) {
			case CONFIGURED:
				return DEPLOYMENT_INSTALLING;
			case INSTALLED:
				return OPERATION_RUNNING;
			default:
				return null;
			}
		}
	},

	/**
	 * running configuration script
	 */
	DEPLOYMENT_INSTALLING {
		@Override
		public State execute(Action action) {
			switch (action) {
			case INSTALLED:
				return OPERATION_RUNNING;
			default:
				return null;
			}
		}
	},

	OPERATION_RUNNING {
		@Override
		public State execute(Action action) {
			switch (action) {
			case UNDEPLOYMENT_REQUESTED:
				return UNDEPLOYMENT;
			case TEST_START_REQUESTED:
				return TEST_RUNNING;
			default:
				return null;
			}
		}
	},

	TEST_RUNNING {
		@Override
		public State execute(Action action) {
			switch (action) {
			case TEST_STOP_REQUSTED:
				return OPERATION_RUNNING;
			case UNDEPLOYMENT_REQUESTED:
				return UNDEPLOYMENT;
			default:
				return null;
			}
		}
	},

	UNDEPLOYMENT {
		@Override
		public State execute(Action action) {
			switch (action) {
			case UNDEPLOYED:
				return IDLE;
			default:
				return null;
			}
		}
	},

	// TODO
	/**
	 * the same with STAGING, but for custom configuration action at runtime
	 */
	STAGING_ACTION {
		@Override
		public State execute(Action action) {
			// TODO Auto-generated method stub
			return null;
		}
	},

	ERROR {
		@Override
		public State execute(Action action) {
			// TODO Auto-generated method stub
			return null;
		}

	};

	abstract public State execute(Action action);

}
