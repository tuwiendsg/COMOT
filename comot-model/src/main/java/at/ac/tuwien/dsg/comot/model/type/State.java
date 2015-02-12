package at.ac.tuwien.dsg.comot.model.type;

// mapped to at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState
public enum State {
	/**
	 * a service unit without any instance
	 */
	UNDEPLOYED,

	/**
	 * an instance is waiting for other condition before deployment. E.g, VM is creating by cloud, software is waiting
	 * for VM or waiting for other "connect to"
	 */
	ALLOCATING,

	/**
	 * the deployment command is assigned and waiting pioneer to get that command.
	 */
	STAGING,

	/**
	 * the same with STAGING, but for custom configuration action at runtime
	 */
	STAGING_ACTION,

	/**
	 * VM is initiating, e.g. setting up pioneer, install predefined packages; or software artifact is downloading,
	 * creating workspace, etc.
	 */
	CONFIGURING,

	/**
	 * running configuration script
	 */
	INSTALLING,

	/**
	 * an artifact is running (it is depricated as we consider that instance must reach DEPLOYED when SALSA does not
	 * monitor it)
	 */
	RUNNING,

	/**
	 * done the configuration
	 */
	DEPLOYED,

	/**
	 * any failure from above steps (not implemented completely)
	 */
	ERROR
}
