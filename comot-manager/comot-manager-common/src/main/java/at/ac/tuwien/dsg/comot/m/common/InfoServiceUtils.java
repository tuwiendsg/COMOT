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

import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.type.OsuType;

public class InfoServiceUtils {

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

	public static boolean isStaticEps(OfferedServiceUnit osu) {

		return isEps(osu) && osu.getServiceTemplate() == null;
	}

	public static boolean isDynamicEps(OfferedServiceUnit osu) throws EpsException {

		return isEps(osu) && osu.getServiceTemplate() != null;
	}

	public static boolean isEps(OfferedServiceUnit osu) {
		return osu != null && osu.getType().equals(OsuType.EPS.toString());
	}

}
