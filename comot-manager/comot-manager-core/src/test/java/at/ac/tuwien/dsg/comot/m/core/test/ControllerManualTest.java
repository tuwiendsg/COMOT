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

import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.ConfigConstants;
import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEventModifying;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsT;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCoreInsertData;
import at.ac.tuwien.dsg.comot.m.core.test.utils.LoadGenerator;
import at.ac.tuwien.dsg.comot.m.core.test.utils.TeAgentAdapter;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.DirectiveType;
import at.ac.tuwien.dsg.comot.model.type.State;

public class ControllerManualTest extends AbstractTest {

	private static final Logger LOG = LoggerFactory.getLogger(ControllerManualTest.class);

	protected String serviceId;

	// protected final String INSTANCE_ID = "HelloElasticityNoDB";// "HelloElasticityNoDB";

	protected String staticDeplId;
	protected String staticMonitoringId;
	protected String staticControlId;

	protected TeAgentAdapter agent;
	protected LoadGenerator generator;

	@Before
	public void setUp() throws Exception {

		staticDeplId = infoService.instanceIdOfStaticEps(AppContextCoreInsertData.SALSA_SERVICE_STATIC);
		staticMonitoringId = infoService.instanceIdOfStaticEps(AppContextCoreInsertData.MELA_SERVICE_STATIC);
		staticControlId = infoService.instanceIdOfStaticEps(AppContextCoreInsertData.RSYBL_SERVICE_STATIC);
		agent = new TeAgentAdapter("prototype", env.getProperty(ConfigConstants.BROKER_HOST));
		generator = new LoadGenerator();

		Definitions tosca1 = UtilsCs.loadTosca(UtilsT.TEST_FILE_BASE
				+ "helloElasticity/HelloElasticity_ShortNames.xml");

		serviceId = coordinator.createService(mapperTosca.createModel(tosca1));

		agent.waitForLifeCycleEvent(Action.CREATED);

	}

	@Test
	public void testDeployAndControl() throws Exception {

		// deploy
		assertFalse(deployment.isManaged(serviceId));

		LOG.info("staticDeplId " + staticDeplId);

		coordinator.assignSupportingOsu(serviceId, staticDeplId);
		agent.waitForCustomEvent(EpsEvent.EPS_SUPPORT_ASSIGNED.toString());

		coordinator.startService(serviceId);

		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		assertEquals(State.RUNNING, lcManager.getCurrentState(serviceId, serviceId));
		assertTrue(deployment.isManaged(serviceId));
		assertTrue(deployment.isRunning(serviceId));

		// controler test

		coordinator.assignSupportingOsu(serviceId, staticControlId);

		agent.waitForCustomEvent(EpsEvent.EPS_SUPPORT_ASSIGNED.toString());
		LOG.info("Controller assigned");

		// UtilsTest.sleepSeconds(10);
		// assertTrue(control.isControlled(instanceId));
		// LOG.info("Controller active");
		//
		// generator.startLoadTunel();
		//
		// agent.waitForLifeCycleEvent(Action.ELASTIC_CHANGE_STARTED);
		// LOG.info("Controller ELASTIC_CHANGE_STARTED");
		//
		// generator.stop();

		UtilsT.sleepInfinit();

	}

	@Test
	public void testInsertExistingAndControl() throws Exception {

		// insertExistingRunningInstanceOfThisServiceToSystem("HelloElasticityNoDB");
		insertExistingRunningInstanceOfThisServiceToSystem(serviceId);

		UtilsT.sleepInfinit();
	}

	@Test
	public void testReconfigureEl() throws Exception {

		// insertExistingRunningInstanceOfThisServiceToSystem("HelloElasticityNoDB");
		insertExistingRunningInstanceOfThisServiceToSystem(serviceId);

		UtilsT.sleepSeconds(5);

		CloudService service = infoService.getService(serviceId);
		Navigator nav = new Navigator(service);
		nav.getUnit("EventProc").getDirectives()
				.add(new SyblDirective("aa", DirectiveType.STRATEGY, "STRATEGY CASE responseTime < 7 ms:scaleIn"));

		coordinator.reconfigureElasticity(serviceId, service);

		UtilsT.sleepInfinit();
	}

	@Autowired
	protected RabbitTemplate amqp;

	public void insertExistingRunningInstanceOfThisServiceToSystem(String serviceId) throws Exception {

		LOG.info("serviceId {}", serviceId);

		assertTrue(deployment.isManaged(serviceId));
		assertTrue(deployment.isRunning(serviceId));

		coordinator.assignSupportingOsu(serviceId, staticDeplId);
		coordinator.assignSupportingOsu(serviceId, staticControlId);

		agent.waitForCustomEvent(EpsEvent.EPS_SUPPORT_ASSIGNED.toString());
		agent.waitForCustomEvent(EpsEvent.EPS_SUPPORT_ASSIGNED.toString());

		CloudService instance = infoService.getService(serviceId);

		instance = deployment.refreshStatus(instance);
		Navigator nav = new Navigator(instance);
		String bindingKey;

		// service deployment
		LifeCycleEvent event = new LifeCycleEvent(serviceId, serviceId, Action.DEPLOYMENT_STARTED, "test",
				System.currentTimeMillis());

		bindingKey = serviceId + "." + LifeCycleEvent.class.getSimpleName() + "."
				+ event.getAction() + "." + Type.SERVICE;

		amqp.convertAndSend(Constants.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));

		for (ServiceUnit unit : nav.getAllUnits()) {
			for (UnitInstance uInst : unit.getInstances()) {

				// start deployment instances
				event = new LifeCycleEventModifying(serviceId, uInst.getId(),
						Action.DEPLOYMENT_STARTED, "test", System.currentTimeMillis(), unit.getId(), uInst);

				bindingKey = serviceId + "." + LifeCycleEvent.class.getSimpleName() + "."
						+ event.getAction() + "." + Type.INSTANCE;

				amqp.convertAndSend(Constants.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));

				// finish deployment instances
				event = new LifeCycleEventModifying(serviceId, uInst.getId(), Action.DEPLOYED, "test",
						System.currentTimeMillis(), unit.getId(), uInst);

				bindingKey = serviceId + "." + LifeCycleEvent.class.getSimpleName() + "."
						+ event.getAction() + "." + Type.INSTANCE;

				amqp.convertAndSend(Constants.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));
			}
		}

		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);
	}

}
