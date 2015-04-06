package at.ac.tuwien.dsg.comot.m.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import at.ac.tuwien.dsg.comot.m.common.enums.ComotEvent;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.test.utils.TestAgentAdapter;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.State;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;

public class MonitoringTest extends AbstractTest {

	protected TestAgentAdapter agent;
	protected String serviceId;
	protected String instanceId;

	protected String staticDeplId;
	protected String staticMonitoringId;

	@Before
	public void setUp() throws JAXBException, IOException, ClassNotFoundException, EpsException, BeansException,
			URISyntaxException {

		agent = new TestAgentAdapter("prototype", env.getProperty("uri.broker.host"));

		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tosca/ExampleExecutableOnVM.xml");

		CloudService service = mapperTosca.createModel(tosca1);
		serviceId = coordinator.createCloudService(service);
		instanceId = coordinator.createServiceInstance(serviceId);

		assertFalse(deployment.isManaged(instanceId));

		staticDeplId = infoService.instanceIdOfStaticEps(Constants.SALSA_SERVICE_STATIC);
		staticMonitoringId = infoService.instanceIdOfStaticEps(Constants.MELA_SERVICE_STATIC);

		coordinator.assignSupportingOsu(serviceId, instanceId, staticDeplId);
		coordinator.assignSupportingOsu(serviceId, instanceId, staticMonitoringId);
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

		agent.waitForLifeCycleEvent(Action.START);

		assertTrue(infoService.isOsuAssignedToInstance(instanceId, Constants.MELA_SERVICE_STATIC));
		assertFalse(monitoring.isMonitored(instanceId));

		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		UtilsTest.sleepSeconds(3);

		assertEquals(State.RUNNING, lcManager.getCurrentState(instanceId, serviceId));
		assertTrue(deployment.isRunning(instanceId));

		// check automatically started
		assertTrue(infoService.isOsuAssignedToInstance(instanceId, Constants.MELA_SERVICE_STATIC));
		assertTrue(monitoring.isMonitored(instanceId));

		// manually stop
		coordinator.triggerCustomEvent(
				serviceId, instanceId, staticMonitoringId, ComotEvent.MELA_STOP.toString(), null);

		agent.waitForCustomEvent(ComotEvent.MELA_STOP.toString());
		UtilsTest.sleepSeconds(3);

		assertTrue(infoService.isOsuAssignedToInstance(instanceId, Constants.MELA_SERVICE_STATIC));
		assertFalse(monitoring.isMonitored(instanceId));

		// manually start
		coordinator.triggerCustomEvent(
				serviceId, instanceId, staticMonitoringId, ComotEvent.MELA_START.toString(), null);

		agent.waitForCustomEvent(ComotEvent.MELA_START.toString());
		UtilsTest.sleepSeconds(3);

		assertTrue(infoService.isOsuAssignedToInstance(instanceId, Constants.MELA_SERVICE_STATIC));
		assertTrue(monitoring.isMonitored(instanceId));

		coordinator.stopServiceInstance(serviceId, instanceId);

		// check automatically stopped
		agent.waitForLifeCycleEvent(Action.STOP);
		agent.assertLifeCycleEvent(Action.UNDEPLOYMENT_STARTED);
		agent.assertLifeCycleEvent(Action.UNDEPLOYED);
		UtilsTest.sleepSeconds(3);

		assertTrue(infoService.isOsuAssignedToInstance(instanceId, Constants.MELA_SERVICE_STATIC));
		assertFalse(monitoring.isMonitored(instanceId));
		assertFalse(deployment.isManaged(instanceId));

	}

}
