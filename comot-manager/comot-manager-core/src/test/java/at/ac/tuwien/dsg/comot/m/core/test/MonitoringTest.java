package at.ac.tuwien.dsg.comot.m.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;

import at.ac.tuwien.dsg.comot.m.common.ComotAction;
import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.test.utils.TestAgentAdapter;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;

public class MonitoringTest extends AbstractTest {

	protected final String MONITORING_ID = Constants.MELA_SERVICE_PUBLIC_ID;
	protected final String DEPLOYMENT_ID = Constants.SALSA_SERVICE_PUBLIC_ID;

	protected TestAgentAdapter agent;
	protected String serviceId;
	protected String instanceId;

	@Before
	public void setUp() throws JAXBException, IOException, ClassNotFoundException, EpsException {

		agent = new TestAgentAdapter("prototype", "localhost");

		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tosca/ExampleExecutableOnVM.xml");

		CloudService service = mapperTosca.createModel(tosca1);
		serviceId = coordinator.createCloudService(service);
		instanceId = coordinator.createServiceInstance(serviceId);

		assertFalse(deployment.isManaged(instanceId));

		coordinator.assignSupportingOsu(serviceId, instanceId, DEPLOYMENT_ID);
		coordinator.assignSupportingOsu(serviceId, instanceId, MONITORING_ID);
		coordinator.startServiceInstance(serviceId, instanceId);

	}

	@After
	public void clean() throws EpsException {
		if (deployment.isManaged(instanceId)) {
			UtilsTest.sleepSeconds(10);
			deployment.undeploy(instanceId);
		}
	}

	@Test(timeout = 240000)
	public void testMonitoring() throws EpsException, ComotException, ShutdownSignalException,
			ConsumerCancelledException, JAXBException, InterruptedException, ClassNotFoundException, IOException {

		agent.waitForLifeCycleEvent(Action.STARTED);

		assertTrue(infoService.isOsuAssignedToInstance(instanceId, MONITORING_ID));
		assertFalse(monitoring.isMonitored(instanceId));

		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		UtilsTest.sleepSeconds(3);

		assertEquals(State.RUNNING, lcManager.getCurrentState(instanceId, serviceId));
		assertTrue(deployment.isRunning(instanceId));

		// check automatically started
		assertTrue(infoService.isOsuAssignedToInstance(instanceId, MONITORING_ID));
		assertTrue(monitoring.isMonitored(instanceId));

		// manually stop
		coordinator.triggerCustomEvent(
				serviceId, instanceId, MONITORING_ID, ComotAction.MELA_STOP.toString(), null);

		agent.waitForCustomEvent(ComotAction.MELA_STOP.toString());
		UtilsTest.sleepSeconds(3);

		assertTrue(infoService.isOsuAssignedToInstance(instanceId, MONITORING_ID));
		assertFalse(monitoring.isMonitored(instanceId));

		// manually start
		coordinator.triggerCustomEvent(
				serviceId, instanceId, MONITORING_ID, ComotAction.MELA_START.toString(), null);

		agent.waitForCustomEvent(ComotAction.MELA_START.toString());
		UtilsTest.sleepSeconds(3);

		assertTrue(infoService.isOsuAssignedToInstance(instanceId, MONITORING_ID));
		assertTrue(monitoring.isMonitored(instanceId));

		coordinator.stopServiceInstance(serviceId, instanceId);

		// check automatically stopped
		agent.waitForLifeCycleEvent(Action.STOPPED);
		agent.assertLifeCycleEvent(Action.UNDEPLOYMENT_STARTED);
		agent.assertLifeCycleEvent(Action.UNDEPLOYED);
		UtilsTest.sleepSeconds(3);

		assertTrue(infoService.isOsuAssignedToInstance(instanceId, MONITORING_ID));
		assertFalse(monitoring.isMonitored(instanceId));
		assertFalse(deployment.isManaged(instanceId));

	}

}
