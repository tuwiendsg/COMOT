package at.ac.tuwien.dsg.comot.model.type;


public enum Action {
	
	INSTANCE_CREATION_REQUESTED,
	STARTED,
	DEPLOYMENT_STARTED,
	ALLOCATED, 	// when status staging
	STAGED,		// when status configuring
	CONFIGURED, // when status installing
	INSTALLED,	// when status deployed / running
	STOPPED,
	UNDEPLOYED,
	TEST_STARTED,
	TEST_FINISHED,
	INSTANCE_REMOVAL_REQUESTED;

}
