package at.ac.tuwien.dsg.comot.m.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.springframework.beans.BeansException;

import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.test.utils.TestAgentAdapter;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.State;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;

public class DeploymentTest extends AbstractTest {

	protected String serviceId;
	protected String instanceId;

	protected TestAgentAdapter agent;
	protected String staticDeplId;

	@Before
	public void setUp() throws JAXBException, IOException, ClassNotFoundException, ShutdownSignalException,
			ConsumerCancelledException, InterruptedException, EpsException, BeansException, URISyntaxException {

		staticDeplId = infoService.instanceIdOfStaticEps(Constants.SALSA_SERVICE_STATIC);
		agent = new TestAgentAdapter("prototype", env.getProperty("uri.broker.host"));

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
			ShutdownSignalException, ConsumerCancelledException, InterruptedException, EpsException {

		assertNotNull(infoService.getServiceInstance(serviceId, instanceId));
		assertTrue(lcManager.isInstanceManaged(instanceId));

		coordinator.removeServiceInstance(serviceId, instanceId);

		agent.assertLifeCycleEvent(Action.REMOVED);

		assertNull(infoService.getServiceInstance(serviceId, instanceId));
		assertFalse(lcManager.isInstanceManaged(instanceId));

	}

	@Test(timeout = 300000)
	public void testAssignStartStop() throws IOException, JAXBException, ClassNotFoundException,
			EpsException, ComotException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {

		coordinator.assignSupportingOsu(serviceId, instanceId, staticDeplId);

		agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_REQUESTED.toString());
		agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_ASSIGNED.toString());

		assertFalse(deployment.isManaged(instanceId));
		assertTrue(infoService.isOsuAssignedToInstance(instanceId, Constants.SALSA_SERVICE_STATIC));

		// DEPLOY when passive
		assertEquals(State.PASSIVE, lcManager.getCurrentState(instanceId, serviceId));

		coordinator.startServiceInstance(serviceId, instanceId);

		agent.assertLifeCycleEvent(Action.START);

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

		agent.assertLifeCycleEvent(Action.STOP);
		agent.assertLifeCycleEvent(Action.UNDEPLOYMENT_STARTED);
		agent.assertLifeCycleEvent(Action.UNDEPLOYED);

		// check that really undeployed when PASSIVE
		assertEquals(State.PASSIVE, lcManager.getCurrentState(instanceId, serviceId));
		assertFalse(deployment.isManaged(instanceId));
		assertTrue(infoService.isOsuAssignedToInstance(instanceId, Constants.SALSA_SERVICE_STATIC));

	}

	@Test(timeout = 300000)
	public void testAssignStartUnassign() throws IOException, JAXBException, ClassNotFoundException,
			EpsException, ComotException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {

		coordinator.assignSupportingOsu(serviceId, instanceId, staticDeplId);

		agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_REQUESTED.toString());
		agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_ASSIGNED.toString());

		coordinator.startServiceInstance(serviceId, instanceId);

		agent.assertLifeCycleEvent(Action.START);

		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);

		// check that deployed and UNDEPLOY
		assertEquals(State.RUNNING, lcManager.getCurrentState(instanceId, serviceId));
		assertTrue(deployment.isManaged(instanceId));
		assertTrue(deployment.isRunning(instanceId));

		coordinator.removeAssignmentOfSupportingOsu(serviceId, instanceId, staticDeplId);

		agent.assertCustomEvent(EpsEvent.EPS_SUPPORT_REMOVED.toString());
		agent.assertLifeCycleEvent(Action.UNDEPLOYMENT_STARTED);
		agent.assertLifeCycleEvent(Action.UNDEPLOYED);

		assertEquals(State.PASSIVE, lcManager.getCurrentState(instanceId, serviceId));
		assertFalse(deployment.isManaged(instanceId));
		assertFalse(infoService.isOsuAssignedToInstance(instanceId, Constants.SALSA_SERVICE_STATIC));
	}
}
