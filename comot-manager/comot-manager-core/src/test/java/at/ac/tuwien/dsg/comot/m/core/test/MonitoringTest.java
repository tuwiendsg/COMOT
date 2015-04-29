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
package at.ac.tuwien.dsg.comot.m.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;

import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.ComotEvent;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.test.utils.TestAgentAdapter;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.State;

public class MonitoringTest extends AbstractTest {

	protected TestAgentAdapter agent;
	protected String serviceId;

	protected String staticDeplId;
	protected String staticMonitoringId;

	@Before
	public void setUp() throws Exception {

		agent = new TestAgentAdapter("prototype", env.getProperty("uri.broker.host"));

		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tosca/ExampleExecutableOnVM.xml");

		CloudService service = mapperTosca.createModel(tosca1);
		serviceId = coordinator.createService(mapperTosca.createModel(tosca1));

		assertFalse(deployment.isManaged(serviceId));

		staticDeplId = infoService.instanceIdOfStaticEps(Constants.SALSA_SERVICE_STATIC);
		staticMonitoringId = infoService.instanceIdOfStaticEps(Constants.MELA_SERVICE_STATIC);

		coordinator.assignSupportingOsu(serviceId, staticDeplId);
		coordinator.assignSupportingOsu(serviceId, staticMonitoringId);
		coordinator.startService(serviceId);

	}

	@After
	public void clean() throws EpsException {
		if (deployment.isManaged(serviceId)) {
			UtilsTest.sleepSeconds(10);
			deployment.undeploy(serviceId);
		}
	}

	@Test(timeout = 240000)
	public void testMonitoring() throws Exception {

		agent.waitForLifeCycleEvent(Action.START);

		assertTrue(infoService.isOsuAssignedToService(serviceId, Constants.MELA_SERVICE_STATIC));
		assertFalse(monitoring.isMonitored(serviceId));

		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		UtilsTest.sleepSeconds(3);

		assertEquals(State.RUNNING, lcManager.getCurrentState(serviceId, serviceId));
		assertTrue(deployment.isRunning(serviceId));

		// check automatically started
		assertTrue(infoService.isOsuAssignedToService(serviceId, Constants.MELA_SERVICE_STATIC));
		assertTrue(monitoring.isMonitored(serviceId));

		// manually stop
		coordinator.triggerCustomEvent(
				serviceId, staticMonitoringId, ComotEvent.MELA_STOP.toString(), null);

		agent.waitForCustomEvent(ComotEvent.MELA_STOP.toString());
		UtilsTest.sleepSeconds(3);

		assertTrue(infoService.isOsuAssignedToService(serviceId, Constants.MELA_SERVICE_STATIC));
		assertFalse(monitoring.isMonitored(serviceId));

		// manually start
		coordinator.triggerCustomEvent(
				serviceId, staticMonitoringId, ComotEvent.MELA_START.toString(), null);

		agent.waitForCustomEvent(ComotEvent.MELA_START.toString());
		UtilsTest.sleepSeconds(3);

		assertTrue(infoService.isOsuAssignedToService(serviceId, Constants.MELA_SERVICE_STATIC));
		assertTrue(monitoring.isMonitored(serviceId));

		coordinator.stopService(serviceId);

		// check automatically stopped
		agent.waitForLifeCycleEvent(Action.STOP);
		agent.assertLifeCycleEvent(Action.UNDEPLOYMENT_STARTED);
		agent.assertLifeCycleEvent(Action.UNDEPLOYED);
		UtilsTest.sleepSeconds(3);

		assertTrue(infoService.isOsuAssignedToService(serviceId, Constants.MELA_SERVICE_STATIC));
		assertFalse(monitoring.isMonitored(serviceId));
		assertFalse(deployment.isManaged(serviceId));

	}

}
