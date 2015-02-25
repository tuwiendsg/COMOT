package at.ac.tuwien.dsg.comot.model.type;


public enum Action {
	NEW_INSTANCE_REQUESTED,
	PREPARED,
	DEPLOYMENT_REQUESTED,
	ALLOCATED, 	// when status staging
	STAGED,		// when status configuring
	CONFIGURED, // when status installing
	INSTALLED,	// when status deployed / running
	UNDEPLOYMENT_REQUESTED,
	UNDEPLOYED,
	TEST_START_REQUESTED,
	TEST_STOP_REQUSTED,
	INSTANCE_REMOVAL_REQUESTED;
	

}
