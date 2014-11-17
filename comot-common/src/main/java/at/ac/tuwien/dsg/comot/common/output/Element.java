package at.ac.tuwien.dsg.comot.common.output;

public class Element {

	protected String id;
	protected Type type;
	protected State state;

	enum Type {
		SERVICE,
		TOPOLOGY,
		OS, WAR, DOCKER, TOMCAT, SOFTWARE,
		INSTANCE
	}

	enum State {
		UNDEPLOYED, ALLOCATING, STAGING, STAGING_ACTION, CONFIGURING, RUNNING, DEPLOYED, ERROR
	}
}
