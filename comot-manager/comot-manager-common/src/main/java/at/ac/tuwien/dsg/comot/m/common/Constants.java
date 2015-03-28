package at.ac.tuwien.dsg.comot.m.common;

public class Constants {

	/**
	 * Original Key pattern: instanceID.changeTRUE/FALSE.stateBefore.stateAfter.eventName.targetLevel.originId Key
	 * pattern: instanceID.eventName.targetLevel.changeTRUE/FALSE.stateBefore.stateAfter.originId
	 */
	public static final String EXCHANGE_LIFE_CYCLE = "EXCHANGE_LIFE_CYCLE";

	/**
	 * Original Key pattern: instanceID.epsId.customEvent.targetLevel Key pattern:
	 * instanceID.eventName.targetLevel.targetId
	 */
	public static final String EXCHANGE_CUSTOM_EVENT = "EXCHANGE_CUSTOM_EVENT";

	/**
	 * Key pattern: instanceID.EventType.eventName.targetLevel
	 */
	public static final String EXCHANGE_REQUESTS = "EXCHANGE_REQUESTS";

	public static final String EXCHANGE_DYNAMIC_REGISTRATION = "EXCHANGE_DYNAMIC_REGISTRATION";

	/**
	 * Key pattern: instanceID.originId
	 */
	public static final String EXCHANGE_EXCEPTIONS = "EXCHANGE_EXCEPTIONS";

	public static final String TYPE_ACTION = "TYPE_ACTION";

	public static final String SALSA_SERVICE_STATIC = "SALSA_SERVICE";
	public static final String MELA_SERVICE_STATIC = "MELA_SERVICE";
	public static final String RSYBL_SERVICE_STATIC = "RSYBL_SERVICE";
	public static final String MELA_SERVICE_DYNAMIC = "MELA_SERVICE_DYNAMIC";
	public static final String RSYBL_SERVICE_DYNAMIC = "RSYBL_SERVICE_DYNAMIC";

	public static final String RECORDER = "RECORDER";
	public static final String EPS_BUILDER = "EPS_BUILDER";

	public static final String ADAPTER_CLASS = "ADAPTER_CLASS";
	public static final String IP = "IP";
	public static final String PORT = "PORT";
	public static final String VIEW = "VIEW";
	public static final String PLACE_HOLDER_INSTANCE_ID = "{PLACE_HOLDER_INSTANCE_ID}";

	public static final String SERVICES = "services";
	public static final String SERVICE_ONE = "services/{serviceId}";
	public static final String INSTANCES = SERVICE_ONE + "/instances";
	public static final String INSTANCE_ONE = SERVICE_ONE + "/instances/{instanceId}";
	public static final String UNIT_INSTANCE_ONE = INSTANCE_ONE + "/units/{unitId}/unitInstances/{unitInstanceId}";

	public static final String EPS_INSTANCE_ASSIGNMENT = INSTANCE_ONE + "/assignedEpses/{epsId}";

	public static final String EPSES = "epses";
	public static final String EPS_ONE_INSTANCES = "epses/{epsId}/instances";

	public static final String EPS_INSTANCES_ALL = "epsesInstances";
	public static final String EPS_INSTANCE_ONE = "epsesInstances/{epsInstanceId}";

	public static final String DELETE_ALL = "all";
}
