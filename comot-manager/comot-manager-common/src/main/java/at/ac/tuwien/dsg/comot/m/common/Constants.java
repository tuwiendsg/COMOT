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
	 * Key pattern: serviceId.eventName.targetLevel.changeTRUE/FALSE.stateBefore.stateAfter.originId
	 */
	public static final String EXCHANGE_LIFE_CYCLE = "EXCHANGE_LIFE_CYCLE";

	/**
	 * Key pattern: serviceId.eventName.targetLevel.targetParticipantId
	 */
	public static final String EXCHANGE_CUSTOM_EVENT = "EXCHANGE_CUSTOM_EVENT";

	/**
	 * Key pattern: serviceId.originId
	 */
	public static final String EXCHANGE_EXCEPTIONS = "EXCHANGE_EXCEPTIONS";

	/**
	 * Key pattern: serviceId.EventType.eventName.groupId
	 */
	public static final String EXCHANGE_REQUESTS = "EXCHANGE_REQUESTS";

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

}
