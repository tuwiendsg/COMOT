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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.ConfigConstants;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsT;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCoreInsertData;
import at.ac.tuwien.dsg.comot.m.core.test.utils.TeAgentAdapter;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.State;

public class DeploymentManualTest extends AbstractTest {

	private static final Logger LOG = LoggerFactory.getLogger(DeploymentManualTest.class);

	protected String serviceId;

	protected TeAgentAdapter agent;
	protected String staticDeplId;

	@Before
	public void setUp() throws Exception {

		UtilsT.sleepSeconds(1);

		staticDeplId = infoService.instanceIdOfStaticEps(AppContextCoreInsertData.SALSA_SERVICE_STATIC);
		agent = new TeAgentAdapter("prototype", env.getProperty(ConfigConstants.BROKER_HOST));

		// Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tomcat/tomcat_from_salsa.xml");
		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tosca/ExampleExecutableOnVM.xml");

		CloudService service = mapperTosca.createModel(tosca1);
		serviceId = coordinator.createService(mapperTosca.createModel(tosca1));

		assertFalse(deployment.isManaged(serviceId));

		agent.waitForLifeCycleEvent(Action.CREATED);

	}

	@After
	public void clean() throws EpsException {

		if (serviceId != null && deployment.isManaged(serviceId)) {
			UtilsT.sleepSeconds(10);
			deployment.undeploy(serviceId);
		}
	}

	@Test
	public void removeServiceInstance() throws Exception {

		assertNotNull(infoService.getService(serviceId));
		assertTrue(lcManager.isInstanceManaged(serviceId));

		coordinator.removeService(serviceId);

		agent.assertLifeCycleEvent(Action.REMOVED);

		assertNull(infoService.getService(serviceId));
		assertFalse(lcManager.isInstanceManaged(serviceId));

	}

	@Test(timeout = 300000)
	public void testAssignStartStop() throws Exception {

		coordinator.assignSupportingOsu(serviceId, staticDeplId);

		agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_REQUESTED.toString());
		agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_ASSIGNED.toString());

		assertFalse(deployment.isManaged(serviceId));
		assertTrue(infoService.isOsuAssignedToService(serviceId, AppContextCoreInsertData.SALSA_SERVICE_STATIC));

		// DEPLOY when passive
		assertEquals(State.PASSIVE, lcManager.getCurrentState(serviceId, serviceId));

		coordinator.startService(serviceId);

		agent.assertLifeCycleEvent(Action.START);

		agent.assertLifeCycleEvent(Action.DEPLOYMENT_STARTED);

		agent.waitForLifeCycleEvent(Action.DEPLOYMENT_STARTED);
		agent.waitForLifeCycleEvent(Action.DEPLOYMENT_STARTED);

		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);

		// check that deployed and UNDEPLOY
		assertEquals(State.RUNNING, lcManager.getCurrentState(serviceId, serviceId));
		assertTrue(deployment.isManaged(serviceId));
		assertTrue(deployment.isRunning(serviceId));

		coordinator.stopService(serviceId);

		agent.assertLifeCycleEvent(Action.STOP);
		agent.assertLifeCycleEvent(Action.UNDEPLOYMENT_STARTED);
		agent.assertLifeCycleEvent(Action.UNDEPLOYED);

		// check that really undeployed when PASSIVE
		assertEquals(State.PASSIVE, lcManager.getCurrentState(serviceId, serviceId));
		assertFalse(deployment.isManaged(serviceId));
		assertTrue(infoService.isOsuAssignedToService(serviceId, AppContextCoreInsertData.SALSA_SERVICE_STATIC));

	}

	@Test(timeout = 300000)
	public void testAssignStartUnassign() throws Exception {

		coordinator.assignSupportingOsu(serviceId, staticDeplId);

		agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_REQUESTED.toString());
		agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_ASSIGNED.toString());

		coordinator.startService(serviceId);

		agent.assertLifeCycleEvent(Action.START);

		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);

		// check that deployed and UNDEPLOY
		assertEquals(State.RUNNING, lcManager.getCurrentState(serviceId, serviceId));
		assertTrue(deployment.isManaged(serviceId));
		assertTrue(deployment.isRunning(serviceId));

		coordinator.removeAssignmentOfSupportingOsu(serviceId, staticDeplId);

		agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_REMOVED.toString());
		agent.assertLifeCycleEvent(Action.UNDEPLOYMENT_STARTED);
		agent.assertLifeCycleEvent(Action.UNDEPLOYED);

		assertEquals(State.PASSIVE, lcManager.getCurrentState(serviceId, serviceId));
		assertFalse(deployment.isManaged(serviceId));
		assertFalse(infoService.isOsuAssignedToService(serviceId, AppContextCoreInsertData.SALSA_SERVICE_STATIC));
	}
}
