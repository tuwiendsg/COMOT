package at.ac.tuwien.dsg.comot.m.core.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.oasis.tosca.Definitions;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.Coordinator;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.type.State;

public class LifecycleTest extends AbstractTest {

	@Autowired
	protected LifeCycleManager lcManager;
	@Autowired
	protected Coordinator coordinator;
	@Autowired
	protected InformationServiceMock infoService;
	
//	protected WrappingNeoServerBootstrapper srv;
//
//	@Before
//	public void setUp() {
//		// http://neo4j.com/docs/1.8.3/server-embedded.html
//		// http://127.0.0.1:7474/
//		srv = new WrappingNeoServerBootstrapper((GraphDatabaseAPI) db);
//		srv.start();
//	}
//
//	@After
//	public void cleanUp() {
//		srv.stop();
//	}


	@Test
	public void produceEvent() throws JAXBException, IOException, ClassNotFoundException {
		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tomcat/tomcat_from_salsa.xml");
		CloudService service = mapperTosca.createModel(tosca1);

		String serviceId = coordinator.createCloudService(service);
		String instanceId = coordinator.createServiceInstance(serviceId);

		UtilsTest.sleepInfinit();
	}

	@Test
	public void testDeployTomcat() throws IOException, JAXBException, ClassNotFoundException {

		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tomcat/tomcat_from_salsa.xml");
		CloudService service = mapperTosca.createModel(tosca1);

		String serviceId = coordinator.createCloudService(service);
		String instanceId = coordinator.createServiceInstance(serviceId);

	
		coordinator.startServiceInstance(serviceId, instanceId);

		coordinator.assignSupportingOsu(serviceId, instanceId, InformationServiceMock.SALSA_SERVICE_PUBLIC_ID);

		
		while (lcManager.getCurrentState(instanceId, serviceId) != State.OPERATION_RUNNING) {
			UtilsTest.sleepSeconds(10);
		}

		coordinator.stopServiceInstance(serviceId, instanceId);
		
		

		log.info("{}", Utils.asJsonString(infoService.getService(serviceId)));

		
		UtilsTest.sleepSeconds(5);
		
		// UtilsTest.sleepInfinit();

	}
}
