package at.ac.tuwien.dsg.comot.model.type;

public enum State {

	INIT {
		@Override
		public State execute(Action action) {
			switch (action) {
			case INSTANCE_CREATED:
				return PASSIVE;
			default:
				return null;
			}
		}
	},

	PASSIVE {
		@Override
		public State execute(Action action) {
			switch (action) {
			case STARTED:
				return STARTING;
			case INSTANCE_REMOVED:
				return FINAL;
			default:
				return null;
			}
		}
	},

	STARTING {
		@Override
		public State execute(Action action) {
			switch (action) {
			case DEPLOYMENT_STARTED:
				return DEPLOYMENT_ALLOCATING;
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
			case STOPPED:
				return STOPPING;
			case TEST_STARTED:
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
			case TEST_FINISHED:
				return OPERATION_RUNNING;
			case STOPPED:
				return STOPPING;
			default:
				return null;
			}
		}
	},

	STOPPING {
		@Override
		public State execute(Action action) {
			switch (action) {
			case UNDEPLOYED:
				return PASSIVE;
			default:
				return null;
			}
		}
	},

	FINAL {
		@Override
		public State execute(Action action) {
			switch (action) {
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
