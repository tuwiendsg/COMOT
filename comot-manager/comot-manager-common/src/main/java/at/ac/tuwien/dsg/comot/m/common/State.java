package at.ac.tuwien.dsg.comot.m.common;

public enum State {

	NONE {
		@Override
		public State execute(Action action) {
			switch (action) {
			case CREATE_NEW_INSTANCE:
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
			case DEPLOY:
				return DEPLOYMENT;
			case REMOVE_INSTANCE:
				return NONE;
			default:
				return null;
			}

		}
	},
	DEPLOYMENT {
		@Override
		public State execute(Action action) {
			switch (action) {
			case SUCCESSFULY_DEPLOYED:
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
			case UNDEPLOY:
				return UNDEPLOYMENT;
			case START_TEST:
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
			case STOP_TEST:
				return OPERATION_RUNNING;
			case UNDEPLOY:
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
			case SUCCESSFULY_UNDEPLOYED:
				return IDLE;
			default:
				return null;
			}
		}
	};

	abstract public State execute(Action action);

}
