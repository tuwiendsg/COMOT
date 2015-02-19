package at.ac.tuwien.dsg.comot.m.core.lifecycle;

public enum State {

	IDLE {
		@Override
		State execute(Action action) {
			switch (action) {
			case DEPLOY:
				return DEPLOYMENT;
			default:
				return null;
			}

		}
	},
	DEPLOYMENT {
		@Override
		State execute(Action action) {
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
		State execute(Action action) {
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
		State execute(Action action) {
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
		State execute(Action action) {
			switch (action) {
			case SUCCESSFULY_UNDEPLOYED:
				return IDLE;
			default:
				return null;
			}
		}
	};

	abstract State execute(Action action);
	
}
