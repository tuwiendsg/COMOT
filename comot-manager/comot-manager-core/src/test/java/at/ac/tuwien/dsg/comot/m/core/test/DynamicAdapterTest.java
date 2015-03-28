package at.ac.tuwien.dsg.comot.m.core.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.amqp.AmqpException;

import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.test.utils.TestAgentAdapter;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;

public class DynamicAdapterTest extends AbstractTest {

	protected TestAgentAdapter agent;
	protected String serviceId;
	protected String instanceId;
	protected String staticDeplId;

	@Before
	public void setUp() throws JAXBException, IOException, ClassNotFoundException, ShutdownSignalException,
			ConsumerCancelledException, InterruptedException, EpsException {

		agent = new TestAgentAdapter("prototype", env.getProperty("uri.broker.host"));

		// Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tosca/ExampleExecutableOnVM.xml");
		//
		// CloudService service = mapperTosca.createModel(tosca1);
		// serviceId = coordinator.createCloudService(service);
		// instanceId = coordinator.createServiceInstance(serviceId);
		//
		//
		// agent.assertLifeCycleEvent(Action.CREATED);

		staticDeplId = infoService.instanceIdOfStaticEps(Constants.SALSA_SERVICE_STATIC);

	}

	@Test
	public void testDynamic() throws EpsException, AmqpException, JAXBException {

		OfferedServiceUnit melaOsu = infoService.getOsu(Constants.MELA_SERVICE_DYNAMIC);

		coordinator.createDynamicService(melaOsu.getId());

		UtilsTest.sleepInfinit();

	}
}
