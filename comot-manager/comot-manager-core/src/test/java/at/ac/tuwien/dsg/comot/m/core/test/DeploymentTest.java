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

import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.test.utils.TestAgentAdapter;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.State;

public class DeploymentTest extends AbstractTest {

	protected String serviceId;

	protected TestAgentAdapter agent;
	protected String staticDeplId;

	@Before
	public void setUp() throws Exception {

		staticDeplId = infoService.instanceIdOfStaticEps(Constants.SALSA_SERVICE_STATIC);
		agent = new TestAgentAdapter("prototype", env.getProperty("uri.broker.host"));

		// Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tomcat/tomcat_from_salsa.xml");
		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tosca/ExampleExecutableOnVM.xml");

		CloudService service = mapperTosca.createModel(tosca1);
		serviceId = coordinator.createService(mapperTosca.createModel(tosca1));

		assertFalse(deployment.isManaged(serviceId));

		agent.assertLifeCycleEvent(Action.CREATED);

	}

	@After
	public void clean() throws EpsException {
		if (deployment.isManaged(serviceId)) {
			UtilsTest.sleepSeconds(10);
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
		assertTrue(infoService.isOsuAssignedToService(serviceId, Constants.SALSA_SERVICE_STATIC));

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
		assertTrue(infoService.isOsuAssignedToService(serviceId, Constants.SALSA_SERVICE_STATIC));

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
		assertFalse(infoService.isOsuAssignedToService(serviceId, Constants.SALSA_SERVICE_STATIC));
	}
}
