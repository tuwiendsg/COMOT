package at.ac.tuwien.dsg.comot.m.core.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
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

	CloudService service;
	String serviceId;
	String instanceId;

	@Before
	public void setUp() throws JAXBException, IOException, ClassNotFoundException {
		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tomcat/tomcat_from_salsa.xml");
		service = mapperTosca.createModel(tosca1);
		serviceId = coordinator.createCloudService(service);
		instanceId = coordinator.createServiceInstance(serviceId);
	}

	@Test
	public void produceEvent() throws JAXBException, IOException, ClassNotFoundException {

		UtilsTest.sleepInfinit();
	}

	@Test
	public void testDeployTomcat() throws IOException, JAXBException, ClassNotFoundException {

		coordinator.startServiceInstance(serviceId, instanceId);
		coordinator.assignSupportingOsu(serviceId, instanceId, InformationServiceMock.SALSA_SERVICE_PUBLIC_ID);

		while (lcManager.getCurrentState(instanceId, serviceId) != State.RUNNING) {
			UtilsTest.sleepSeconds(10);
		}

		coordinator.stopServiceInstance(serviceId, instanceId);

		log.info("{}", Utils.asJsonString(infoService.getService(serviceId)));

		UtilsTest.sleepSeconds(5);
		// UtilsTest.sleepInfinit();
	}

	@Test
	public void testDeployTomcatNormal() throws IOException, JAXBException, ClassNotFoundException {

		coordinator.assignSupportingOsu(serviceId, instanceId, InformationServiceMock.SALSA_SERVICE_PUBLIC_ID);
		coordinator.startServiceInstance(serviceId, instanceId);

		while (lcManager.getCurrentState(instanceId, serviceId) != State.RUNNING) {
			UtilsTest.sleepSeconds(10);
		}

		coordinator.stopServiceInstance(serviceId, instanceId);

		log.info("{}", Utils.asJsonString(infoService.getService(serviceId)));

		UtilsTest.sleepSeconds(5);
		// UtilsTest.sleepInfinit();
	}
}
