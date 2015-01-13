package at.ac.tuwien.dsg.comot.model.type;

// mapped to at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState
public enum State {
	UNDEPLOYED,
	ALLOCATING,
	STAGING,
	STAGING_ACTION,
	CONFIGURING,
	INSTALLING,
	RUNNING,
	DEPLOYED,
	ERROR
}
