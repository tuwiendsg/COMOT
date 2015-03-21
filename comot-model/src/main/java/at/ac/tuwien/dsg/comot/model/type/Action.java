package at.ac.tuwien.dsg.comot.model.type;


public enum Action {
	
	CREATED,
	STARTED,
	DEPLOYMENT_STARTED,
	DEPLOYED,	// when status deployed / running
	ELASTIC_CHANGE_STARTED,
	ELASTIC_CHANGE_FINISHED,
	UPDATE_STARTED,
	UPDATE_FINISHED,
	STOPPED,
	UNDEPLOYMENT_STARTED,
	UNDEPLOYED,
	REMOVED,
	ERROR,
	
	ALLOCATED, 	// when status staging
	STAGED,		// when status configuring
	CONFIGURED, // when status installing	
	
	RECONFIGURED_ELASTICITY,
	TEST_STARTED,
	TEST_FINISHED,
	CONTROLLER_STARTED,
	CONTROLLER_STOPPED,
	UPDATE;

}
