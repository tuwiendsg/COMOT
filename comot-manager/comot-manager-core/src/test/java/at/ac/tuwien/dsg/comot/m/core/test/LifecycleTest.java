package at.ac.tuwien.dsg.comot.m.core.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.Coordinator;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

public class LifecycleTest extends AbstractTest {

	@Autowired
	LifeCycleManager cycle;
	@Autowired
	Coordinator orchestrator;

	@Test
	public void produceEvent() throws JAXBException, IOException, ClassNotFoundException {
		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tomcat/tomcat_from_salsa.xml");
		CloudService service = mapperTosca.createModel(tosca1);

		String serviceId = orchestrator.createCloudService(service);

		String instanceId = orchestrator.createServiceInstance(serviceId);

		UtilsTest.sleepInfinit();
	}

	@Test
	public void testDeployTomcat() throws IOException, JAXBException, ClassNotFoundException {

		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tomcat/tomcat_from_salsa.xml");
		CloudService service = mapperTosca.createModel(tosca1);

		String serviceId = orchestrator.createCloudService(service);
		String instanceId = orchestrator.createServiceInstance(serviceId);

		orchestrator.assignSupportingOsu(instanceId, InformationServiceMock.SALSA_SERVICE_PUBLIC_ID);

		UtilsTest.sleepInfinit();

	}
}
