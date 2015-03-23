package at.ac.tuwien.dsg.comot.m.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;

import at.ac.tuwien.dsg.comot.m.common.EpsAction;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.test.utils.TestAgentAdapter;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;

public class DeploymentTest extends AbstractTest {

	protected final String MONITORING_ID = InformationServiceMock.MELA_SERVICE_PUBLIC_ID;
	protected final String DEPLOYMENT_ID = InformationServiceMock.SALSA_SERVICE_PUBLIC_ID;

	protected TestAgentAdapter agent;
	protected String serviceId;
	protected String instanceId;

	@Before
	public void setUp() throws JAXBException, IOException, ClassNotFoundException, ShutdownSignalException,
			ConsumerCancelledException, InterruptedException, EpsException {

		agent = new TestAgentAdapter("prototype", "localhost");

		// Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tomcat/tomcat_from_salsa.xml");
		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tosca/ExampleExecutableOnVM.xml");

		CloudService service = mapperTosca.createModel(tosca1);
		serviceId = coordinator.createCloudService(service);
		instanceId = coordinator.createServiceInstance(serviceId);

		assertFalse(deployment.isManaged(instanceId));

		agent.assertLifeCycleEvent(Action.CREATED);
	}

	@After
	public void clean() throws EpsException {
		if (deployment.isManaged(instanceId)) {
			UtilsTest.sleepSeconds(10);
			deployment.undeploy(instanceId);
		}
	}

	@Test
	public void removeServiceInstance() throws ClassNotFoundException, IOException, JAXBException,
			ShutdownSignalException, ConsumerCancelledException, InterruptedException {

		assertNotNull(infoService.getServiceInstance(instanceId));
		assertTrue(lcManager.isInstanceManaged(instanceId));

		coordinator.removeServiceInstance(serviceId, instanceId);

		agent.assertLifeCycleEvent(Action.REMOVED);

		assertNull(infoService.getServiceInstance(instanceId));
		assertFalse(lcManager.isInstanceManaged(instanceId));

	}

	@Test(timeout = 240000)
	public void testAssignStartStop() throws IOException, JAXBException, ClassNotFoundException,
			EpsException, ComotException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {

		coordinator.assignSupportingOsu(serviceId, instanceId, DEPLOYMENT_ID);

		agent.assertCustomEvent(EpsAction.EPS_ASSIGNMENT_REQUESTED.toString());
		agent.assertCustomEvent(EpsAction.EPS_ASSIGNED.toString());

		assertFalse(deployment.isManaged(instanceId));
		assertTrue(infoService.isOsuAssignedToInstance(instanceId, DEPLOYMENT_ID));

		// DEPLOY when passive
		assertEquals(State.PASSIVE, lcManager.getCurrentState(instanceId, serviceId));

		coordinator.startServiceInstance(serviceId, instanceId);

		agent.assertLifeCycleEvent(Action.STARTED);

		agent.assertLifeCycleEvent(Action.DEPLOYMENT_STARTED);

		agent.waitForLifeCycleEvent(Action.DEPLOYMENT_STARTED);
		agent.waitForLifeCycleEvent(Action.DEPLOYMENT_STARTED);

		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);

		// check that deployed and UNDEPLOY
		assertEquals(State.RUNNING, lcManager.getCurrentState(instanceId, serviceId));
		assertTrue(deployment.isManaged(instanceId));
		assertTrue(deployment.isRunning(instanceId));

		coordinator.stopServiceInstance(serviceId, instanceId);

		agent.assertLifeCycleEvent(Action.STOPPED);
		agent.assertLifeCycleEvent(Action.UNDEPLOYMENT_STARTED);
		agent.assertLifeCycleEvent(Action.UNDEPLOYED);

		// check that really undeployed when PASSIVE
		assertEquals(State.PASSIVE, lcManager.getCurrentState(instanceId, serviceId));
		assertFalse(deployment.isManaged(instanceId));
		assertTrue(infoService.isOsuAssignedToInstance(instanceId, DEPLOYMENT_ID));

	}

	@Test(timeout = 240000)
	public void testAssignStartUnassign() throws IOException, JAXBException, ClassNotFoundException,
			EpsException, ComotException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {

		coordinator.assignSupportingOsu(serviceId, instanceId, DEPLOYMENT_ID);

		agent.assertCustomEvent(EpsAction.EPS_ASSIGNMENT_REQUESTED.toString());
		agent.assertCustomEvent(EpsAction.EPS_ASSIGNED.toString());

		coordinator.startServiceInstance(serviceId, instanceId);

		agent.assertLifeCycleEvent(Action.STARTED);

		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);

		// check that deployed and UNDEPLOY
		assertEquals(State.RUNNING, lcManager.getCurrentState(instanceId, serviceId));
		assertTrue(deployment.isManaged(instanceId));
		assertTrue(deployment.isRunning(instanceId));

		coordinator.removeAssignmentOfSupportingOsu(serviceId, instanceId, DEPLOYMENT_ID);

		agent.assertCustomEvent(EpsAction.EPS_ASSIGNMENT_REMOVED.toString());
		agent.assertLifeCycleEvent(Action.UNDEPLOYMENT_STARTED);
		agent.assertLifeCycleEvent(Action.UNDEPLOYED);

		assertEquals(State.PASSIVE, lcManager.getCurrentState(instanceId, serviceId));
		assertFalse(deployment.isManaged(instanceId));
		assertFalse(infoService.isOsuAssignedToInstance(instanceId, DEPLOYMENT_ID));
	}
}
