package at.ac.tuwien.dsg.comot.cs.test.clients;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityType;
import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.cs.connector.SalsaClient;
import at.ac.tuwien.dsg.comot.cs.test.AbstractTest;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

public class SalsaClientTest extends AbstractTest {

	public static final String SERVICE_ID = "example_deployOneVM";
	public static final String TOPOLOGY_ID = "example_topology";
	public static final String NODE_ID = "example_OS_comot";

	@Autowired
	private SalsaClient salsa;
	private String xmlTosca;

	@Before
	public void setup() throws IOException {
		xmlTosca = Utils.loadFile("./../resources/test/xml/ExampleDeployOneVM.xml");
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testAutomated() throws CoreServiceException, InterruptedException, ComotException {

		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService service;
		int countIter = 0;

		// deploy
		salsa.deploy(xmlTosca);

		do {

			Thread.sleep(10000);
			service = salsa.getStatus(SERVICE_ID);

			if (countIter > 60) {
				countIter = 0;
				break;
			}

		} while (!(service.getState().equals(SalsaEntityState.DEPLOYED)));

		// check DeploymentInfo
		assertNotNull(salsa.getServiceDeploymentInfo(SERVICE_ID));

		int vm_count = service.getAllReplicaByType(SalsaEntityType.OPERATING_SYSTEM).size();
		assertEquals(1, vm_count);

		// spawn 2 new instances
		salsa.spawn(SERVICE_ID, TOPOLOGY_ID, NODE_ID, 1);

		do {

			Thread.sleep(10000);
			service = salsa.getStatus(SERVICE_ID);

			if (countIter > 60) {
				countIter = 0;
				break;
			}

		} while (2 != service.getAllReplicaByType(SalsaEntityType.OPERATING_SYSTEM).size());

		vm_count = service.getAllReplicaByType(SalsaEntityType.OPERATING_SYSTEM).size();
		assertEquals(2, vm_count);

		// destroy one instance
		salsa.destroy(SERVICE_ID, TOPOLOGY_ID, NODE_ID, 1);

		do {

			Thread.sleep(10000);
			service = salsa.getStatus(SERVICE_ID);

			if (countIter > 12) {
				countIter = 0;
				break;
			}

		} while (1 != service.getAllReplicaByType(SalsaEntityType.OPERATING_SYSTEM).size());

		vm_count = service.getAllReplicaByType(SalsaEntityType.OPERATING_SYSTEM).size();
		assertEquals(1, vm_count);

		// undeploy
		salsa.undeploy(SERVICE_ID);

		try {
			do {

				Thread.sleep(10000);
				service = salsa.getStatus(SERVICE_ID);

				if (countIter > 12) {
					countIter = 0;
					break;
				}

			} while (true);

			fail("Did not undeploy");
		} catch (CoreServiceException e) {
		}

	}

	@Test
	public void testDeploy() throws CoreServiceException {
		salsa.deploy(xmlTosca);
	}

	@Test
	public void testStatus() throws CoreServiceException, JAXBException, ComotException {
		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService serviceInfo = salsa
				.getStatus("SalsaExample_comot");

		log.info(UtilsCs.asString(serviceInfo));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testDeploymentDescription() throws CoreServiceException, JAXBException {
		DeploymentDescription descr = salsa.getServiceDeploymentInfo("aaaa");

		log.info(UtilsCs.asString(descr));
	}

	@Test
	public void testGetTosca() throws CoreServiceException, JAXBException, ComotException {
		Definitions def = salsa.getTosca("aaaa");

		log.info(UtilsCs.asString(def));
	}

	@Test
	public void testUndeploy() throws CoreServiceException {
		salsa.undeploy(SERVICE_ID);
	}

	@Test
	public void testSpawnInstance() throws CoreServiceException {
		salsa.spawn(SERVICE_ID, TOPOLOGY_ID, NODE_ID, 2);
	}

	@Test
	public void testDestroyInstance() throws CoreServiceException {
		salsa.destroy(SERVICE_ID, TOPOLOGY_ID, NODE_ID, 1);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testGetAllServices() throws CoreServiceException, JAXBException, ComotException {
		String def = salsa.getServices();
		log.info(def);
	}
}
