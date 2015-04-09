package at.ac.tuwien.dsg.comot.m.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEvent;
import at.ac.tuwien.dsg.comot.m.common.event.LifeCycleEventModifying;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.test.utils.LoadGenerator;
import at.ac.tuwien.dsg.comot.m.core.test.utils.TestAgentAdapter;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
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
			ConsumerCancelledException, InterruptedException, EpsException, ComotException, BeansException,
			URISyntaxException {

		staticDeplId = infoService.instanceIdOfStaticEps(Constants.SALSA_SERVICE_STATIC);
		staticMonitoringId = infoService.instanceIdOfStaticEps(Constants.MELA_SERVICE_STATIC);
		staticControlId = infoService.instanceIdOfStaticEps(Constants.RSYBL_SERVICE_STATIC);
		agent = new TestAgentAdapter("prototype", env.getProperty("uri.broker.host"));
		generator = new LoadGenerator();

		Definitions tosca1 = UtilsCs.loadTosca(UtilsTest.TEST_FILE_BASE
				+ "helloElasticity/HelloElasticity_ShortNames.xml");

		CloudService service = mapperTosca.createModel(tosca1);
		serviceId = coordinator.createCloudService(service);
		instanceId = coordinator.createServiceInstance(serviceId);

		agent.assertLifeCycleEvent(Action.CREATED);

	}

	@Test
	public void testDeployAndControl() throws IOException, JAXBException, ClassNotFoundException, EpsException,
			ShutdownSignalException, ConsumerCancelledException, InterruptedException, ComotException {

		// deploy
		assertFalse(deployment.isManaged(instanceId));

		log.info("staticDeplId " + staticDeplId);

		coordinator.assignSupportingOsu(serviceId, instanceId, staticDeplId);
		agent.waitForCustomEvent(EpsEvent.EPS_SUPPORT_ASSIGNED.toString());

		coordinator.startServiceInstance(serviceId, instanceId);

		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		agent.waitForLifeCycleEvent(Action.DEPLOYED);
		assertEquals(State.RUNNING, lcManager.getCurrentState(instanceId, serviceId));
		assertTrue(deployment.isManaged(instanceId));
		assertTrue(deployment.isRunning(instanceId));

		// controler test

		coordinator.assignSupportingOsu(serviceId, instanceId, staticControlId);

		agent.waitForCustomEvent(EpsEvent.EPS_SUPPORT_ASSIGNED.toString());
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

	@Test
	public void testInsertExistingAndControl() throws AmqpException, ShutdownSignalException,
			ConsumerCancelledException,
			EpsException, JAXBException, ComotException, IOException, InterruptedException {

		// insertExistingRunningInstanceOfThisServiceToSystem("HelloElasticityNoDB");
		insertExistingRunningInstanceOfThisServiceToSystem(serviceId);

		UtilsTest.sleepInfinit();
	}

	@Autowired
	protected RabbitTemplate amqp;

	public void insertExistingRunningInstanceOfThisServiceToSystem(String serviceId) throws AmqpException,
			EpsException,
			JAXBException, ComotException, IOException, ShutdownSignalException, ConsumerCancelledException,
			InterruptedException {

		log.info("instanceId {}", instanceId);

		assertTrue(deployment.isManaged(instanceId));
		assertTrue(deployment.isRunning(instanceId));

		coordinator.assignSupportingOsu(serviceId, instanceId, staticDeplId);
		coordinator.assignSupportingOsu(serviceId, instanceId, staticControlId);

		agent.waitForCustomEvent(EpsEvent.EPS_SUPPORT_ASSIGNED.toString());
		agent.waitForCustomEvent(EpsEvent.EPS_SUPPORT_ASSIGNED.toString());

		CloudService instance = infoService.getServiceInstance(instanceId);
		instance.setId(instanceId);
		instance.setName(instanceId);

		instance = deployment.refreshStatus(instance);
		Navigator nav = new Navigator(instance);
		String bindingKey;

		// service deployment
		LifeCycleEvent event = new LifeCycleEvent(serviceId, instanceId, serviceId, Action.DEPLOYMENT_STARTED, "test",
				System.currentTimeMillis());

		bindingKey = instanceId + "." + LifeCycleEvent.class.getSimpleName() + "."
				+ event.getAction() + "." + Type.SERVICE;

		amqp.convertAndSend(Constants.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));

		for (ServiceUnit unit : nav.getAllUnits()) {
			for (UnitInstance uInst : unit.getInstances()) {

				// start deployment instances
				event = new LifeCycleEventModifying(serviceId, instanceId, uInst.getId(),
						Action.DEPLOYMENT_STARTED, "test", System.currentTimeMillis(), unit.getId(), uInst);

				bindingKey = instanceId + "." + LifeCycleEvent.class.getSimpleName() + "."
						+ event.getAction() + "." + Type.INSTANCE;

				amqp.convertAndSend(Constants.EXCHANGE_REQUESTS, bindingKey, Utils.asJsonString(event));

				// finish deployment instances
				event = new LifeCycleEventModifying(serviceId, instanceId, uInst.getId(), Action.DEPLOYED, "test",
						System.currentTimeMillis(), unit.getId(), uInst);

				bindingKey = instanceId + "." + LifeCycleEvent.class.getSimpleName() + "."
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
