/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
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

	// public static String bindingLifeCycle(String instanceId, boolean change, State before, State after, Action event,
	// Type target, String originId){
	//
	// }

	public static final String TYPE_ACTION = "TYPE_ACTION";

	public static final String SALSA_SERVICE_STATIC = "SALSA_SERVICE";
	public static final String MELA_SERVICE_STATIC = "MELA_SERVICE";
	public static final String RSYBL_SERVICE_STATIC = "RSYBL_SERVICE";
	public static final String MELA_SERVICE_DYNAMIC = "MELA_SERVICE_USER_MANAGED";
	public static final String RSYBL_SERVICE_DYNAMIC = "RSYBL_SERVICE_USER_MANAGED";
	public static final String SALSA_SERVICE_DYNAMIC = "SALSA_SERVICE_USER_MANAGED";

	public static final String ROLE_DEPLOYER = "Deployer";
	public static final String ROLE_MAINTENANCE = "Maintenance";
	public static final String ROLE_CONTROLLER = "Controller";
	public static final String ROLE_MANAGER = "Manager";
	public static final String ROLE_OBSERVER = "Observer";

	public static final String RECORDER = "RECORDER";
	public static final String EPS_BUILDER = "EPS_BUILDER";

	public static final String ADAPTER_CLASS = "ADAPTER_CLASS";
	public static final String IP = "IP";
	public static final String PORT = "PORT";
	public static final String VIEW = "VIEW";
	public static final String PLACE_HOLDER_INSTANCE_ID = "{PLACE_HOLDER_INSTANCE_ID}";

	public static final String TEMPLATES = "templates";
	public static final String TEMPLATES_ONE = "templates/{templateId}";
	public static final String TEMPLATES_ONE_SERVICES = "templates/{templateId}/services";
	public static final String SERVICES = "services";
	public static final String SERVICE_ONE = "services/{serviceId}";
	public static final String SERVICE_ONE_ELASTICITY = "services/{serviceId}/elasticity";
	public static final String UNIT_INSTANCE_ONE = SERVICE_ONE + "/units/{unitId}/unitInstances/{unitInstanceId}";

	public static final String EPS_INSTANCE_ASSIGNMENT = SERVICE_ONE + "/assignedEpses/{epsId}";

	public static final String EPSES = "epses";
	public static final String EPS_ONE_INSTANCES = "epses/{epsId}/instances";

	public static final String EPS_INSTANCES_ALL = "epsesInstances";
	public static final String EPS_INSTANCE_ONE = "epsesInstances/{epsInstanceId}";

	public static final String DELETE_ALL = "all";

}
