package at.ac.tuwien.dsg.comot.m.core.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.ComotOrchestrator;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.EpsCoordinator;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

public class LifecycleTest extends AbstractTest {

	@Autowired
	LifeCycleManager cycle;
	@Autowired
	EpsCoordinator aaa;
	@Autowired
	ComotOrchestrator orchestrator;

	// @Test
	// public void testCycle() throws IOException, JAXBException {
	//
	// EventMessage event = new EventMessage("serviceId", "instanceId", "groupId", Action.NEW_INSTANCE_REQUESTED,
	// "aaaaaaaaa");
	// StateMessage msg = new StateMessage(event);
	// msg.addOne("ooo", State.NONE, State.IDLE);
	//
	// // CloudService service = STemplates.fullService();
	//
	// // cycle.send("uuu.oooo", msg);
	//
	// UtilsTest.sleepSeconds(2);
	//
	// }

	// @Test
	// public void testDeploy() throws IOException, JAXBException {
	//
	// CloudService service = STemplates.fullService();
	//
	// String serviceId = orchestrator.createCloudService(service);
	//
	// String instanceId = orchestrator.createNewServiceInstance(serviceId);
	//
	// orchestrator.assignDeployment(instanceId);
	//
	// UtilsTest.sleepSeconds(2);
	//
	// }

	@Test
	public void testDeployTomcat() throws IOException, JAXBException {

		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tomcat/tomcat_from_salsa.xml");
		CloudService service = mapperTosca.createModel(tosca1);

		String serviceId = orchestrator.createCloudService(service);

		String instanceId = orchestrator.createNewServiceInstance(serviceId);

		orchestrator.assignDeployment(instanceId);

		UtilsTest.sleepInfinit();

	}
}
