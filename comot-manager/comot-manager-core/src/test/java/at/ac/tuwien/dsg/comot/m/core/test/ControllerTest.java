package at.ac.tuwien.dsg.comot.m.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;

import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.EpsAction;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.test.utils.LoadGenerator;
import at.ac.tuwien.dsg.comot.m.core.test.utils.TestAgentAdapter;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.Action;
import at.ac.tuwien.dsg.comot.model.type.State;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;

public class ControllerTest extends AbstractTest {

	protected String serviceId;
	protected String instanceId;

	// protected final String INSTANCE_ID = "HelloElasticityNoDB";// "HelloElasticityNoDB";

	protected String staticDeplId;
	protected String staticMonitoringId;
	protected String staticControlId;

	protected TestAgentAdapter agent;
	protected LoadGenerator generator;

	@Before
	public void setUp() throws JAXBException, IOException, ClassNotFoundException, ShutdownSignalException,
			ConsumerCancelledException, InterruptedException, EpsException, ComotException {

		staticDeplId = infoService.instanceIdOfStaticEps(Constants.SALSA_SERVICE_STATIC);
		staticMonitoringId = infoService.instanceIdOfStaticEps(Constants.MELA_SERVICE_STATIC);
		staticControlId = infoService.instanceIdOfStaticEps(Constants.RSYBL_SERVICE_STATIC);
		agent = new TestAgentAdapter("prototype", env.getProperty("uri.broker.host"));
		generator = new LoadGenerator();

		Definitions tosca1 = UtilsCs.loadTosca(UtilsTest.TEST_FILE_BASE + "helloElasticity/HelloElasticityNoDB.xml");

		CloudService service = mapperTosca.createModel(tosca1);
		serviceId = coordinator.createCloudService(service);
		instanceId = coordinator.createServiceInstance(serviceId);

		assertFalse(deployment.isManaged(instanceId));
		agent.assertLifeCycleEvent(Action.CREATED);

		coordinator.assignSupportingOsu(serviceId, instanceId, staticDeplId);
		agent.waitForCustomEvent(EpsAction.EPS_SUPPORT_ASSIGNED.toString());

		coordinator.startServiceInstance(serviceId, instanceId);

		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		assertEquals(State.RUNNING, lcManager.getCurrentState(instanceId, serviceId));
		assertTrue(deployment.isManaged(instanceId));
		assertTrue(deployment.isRunning(instanceId));
	}

	@Test
	public void testControl() throws IOException, JAXBException, ClassNotFoundException, EpsException,
			ShutdownSignalException, ConsumerCancelledException, InterruptedException {

		serviceId = infoService.getServiceInstance(instanceId).getId();

		coordinator.assignSupportingOsu(serviceId, instanceId, staticControlId);

		agent.waitForCustomEvent(EpsAction.EPS_SUPPORT_ASSIGNED.toString());
		log.info("Controller assigned");

		// UtilsTest.sleepSeconds(10);
		// assertTrue(control.isControlled(instanceId));
		// log.info("Controller active");
		//
		// generator.startLoadTunel();
		//
		// agent.waitForLifeCycleEvent(Action.ELASTIC_CHANGE_STARTED);
		// log.info("Controller ELASTIC_CHANGE_STARTED");
		//
		// generator.stop();

		UtilsTest.sleepInfinit();

	}

}
