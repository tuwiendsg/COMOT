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
package at.ac.tuwien.dsg.comot.m.recorder.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.State;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;

public class ManualTest extends AbstractTest {

	private static final Logger LOG = LoggerFactory.getLogger(ManualTest.class);

	protected CloudService service;

	@Before
	public void startup() {
		service = STemplates.fullService();
	}

	@Test
	public void testEventProperties() throws IllegalArgumentException, IllegalAccessException, InstantiationException,
			ClassNotFoundException, RecorderException {

		CloudService service = STemplates.fullService();
		cutOsus(service);
		Navigator nav = new Navigator(service);
		String osInstId = STemplates.instanceId(STemplates.osNodeId);

		// change 0 created the instance

		Long time1 = System.currentTimeMillis();
		Map<String, Object> changeProperties = createProps(Action.DEPLOYMENT_STARTED.toString(), time1);

		revisionApi.createOrUpdateRegion(
				service, STemplates.serviceId, osInstId, "Recording.CHANGE_TYPE_LIFECYCLE",
				changeProperties);

		// change 1 still deploying

		nav.getInstance(osInstId).setState(State.DEPLOYING);
		nav.getInstance(osInstId).setEnvId("aaa");

		changeProperties = createProps("CONFIGURING", System.currentTimeMillis());

		revisionApi.createOrUpdateRegion(
				service, STemplates.serviceId, osInstId, "Recording.CHANGE_TYPE_CUSTOM", changeProperties);

		// change 2 finished deployemnt

		nav.getInstance(osInstId).setState(State.RUNNING);
		Long time2 = System.currentTimeMillis();
		changeProperties = createProps(Action.DEPLOYED.toString(), time2);

		revisionApi.createOrUpdateRegion(
				service, STemplates.serviceId, osInstId, "Recording.CHANGE_TYPE_LIFECYCLE",
				changeProperties);

		// change 3

		nav.getInstance(osInstId).setEnvId("bbb");

		changeProperties = createProps("SOME_EVENT", System.currentTimeMillis());

		revisionApi.createOrUpdateRegion(
				service, STemplates.serviceId, osInstId, "Recording.CHANGE_TYPE_LIFECYCLE",
				changeProperties);

	}

	public static Map<String, Object> createProps(String action, Long time) {
		Map<String, Object> changeProperties = new HashMap<>();
		changeProperties.put("Recording.PROP_EVENT_NAME", action);
		changeProperties.put("Recording.PROP_EVENT_TIME", time);
		return changeProperties;
	}

}
