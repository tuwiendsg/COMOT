package at.ac.tuwien.dsg.comot.m.core.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.Coordinator;
import at.ac.tuwien.dsg.comot.m.core.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

public class DeploymentTest extends AbstractTest {

	@Autowired
	protected LifeCycleManager lcManager;
	@Autowired
	protected Coordinator coordinator;
	@Autowired
	protected InformationServiceMock infoService;

	CloudService service;
	String serviceId;
	String instanceId;
	protected final String monitoringId = InformationServiceMock.MELA_SERVICE_PUBLIC_ID;
	protected final String deploymentId = InformationServiceMock.SALSA_SERVICE_PUBLIC_ID;

	@Before
	public void setUp() throws JAXBException, IOException, ClassNotFoundException {
		// Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tomcat/tomcat_from_salsa.xml");
		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/xml/ExampleExecutableOnVM.xml");

		service = mapperTosca.createModel(tosca1);
		serviceId = coordinator.createCloudService(service);
		instanceId = coordinator.createServiceInstance(serviceId);

		UtilsTest.sleepSeconds(5);

	}

	@Test
	public void testCreateRemove() throws ClassNotFoundException, IOException, JAXBException {

		// TODO does not work yet

		assertNotNull(infoService.getServiceInstance(instanceId));
		assertTrue(lcManager.isInstanceManaged(instanceId));

		coordinator.removeServiceInstance(serviceId, instanceId);

		UtilsTest.sleepSeconds(5);

		assertNull(infoService.getServiceInstance(instanceId));
		assertFalse(lcManager.isInstanceManaged(instanceId));

	}

	@Test(timeout = 240000)
	public void testStartAssignStop() throws IOException, JAXBException, ClassNotFoundException,
			EpsException, ComotException {

		boolean wasRunning = false;
		assertFalse(deployment.isManaged(instanceId));

		while (true) {
			switch (lcManager.getCurrentState(instanceId, serviceId)) {

			case PASSIVE:

				if (!wasRunning) {
					coordinator.startServiceInstance(serviceId, instanceId);
					coordinator.assignSupportingOsu(serviceId, instanceId, deploymentId);
				} else {
					assertFalse(deployment.isManaged(instanceId));
					return;
				}
				break;

			case RUNNING:
				assertTrue(deployment.isManaged(instanceId));
				assertTrue(deployment.isRunning(instanceId));
				wasRunning = true;

				coordinator.stopServiceInstance(serviceId, instanceId);
				break;

			case ERROR:
				fail("Should not reach ERROR state");
				break;
			}
			UtilsTest.sleepSeconds(5);
		}
	}

	@Test(timeout = 240000)
	public void testAssignStartUnassign() throws IOException, JAXBException, ClassNotFoundException,
			EpsException, ComotException {

		boolean wasRunning = false;
		assertFalse(deployment.isManaged(instanceId));

		while (true) {
			switch (lcManager.getCurrentState(instanceId, serviceId)) {

			case PASSIVE:

				if (!wasRunning) {
					coordinator.assignSupportingOsu(serviceId, instanceId, deploymentId);
					coordinator.startServiceInstance(serviceId, instanceId);
				} else {
					assertFalse(deployment.isManaged(instanceId));
					return;
				}
				break;

			case RUNNING:
				assertTrue(deployment.isManaged(instanceId));
				assertTrue(deployment.isRunning(instanceId));
				wasRunning = true;

				coordinator.removeAssignmentOfSupportingOsu(serviceId, instanceId, deploymentId);
				break;

			case ERROR:
				fail("Should not reach ERROR state");
				break;
			}
			UtilsTest.sleepSeconds(5);
		}
	}

}
