package at.ac.tuwien.dsg.comot.cs.test.clients;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityType;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.test.TestUtils;
import at.ac.tuwien.dsg.comot.cs.connector.SalsaClient;

public class SalsaClientTest {

	private final static Logger log = LoggerFactory.getLogger(SalsaClientTest.class);

	public static final String SALSA_IP = "128.130.172.215";
	public static final int SALSA_PORT = 8080;

	public static final String SERVICE_ID = "example_deployOneVM";
	public static final String TOPOLOGY_ID = "example_topology";
	public static final String NODE_ID = "example_OS_comot";

	private SalsaClient salsa;
	private String xmlTosca;

	@Before
	public void setup() throws IOException {
		salsa = new SalsaClient(SALSA_IP, SALSA_PORT);
		xmlTosca = TestUtils.loadFile("./xml/ExampleDeployOneVM.xml");
	}

	@After
	public void cleanUp() {
		salsa.close();
	}

	@Test
	public void testAutomated() throws CoreServiceException, InterruptedException {

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

		} while (!(service.getState().equals(SalsaEntityState.DEPLOYED)
		|| service.getState().equals(SalsaEntityState.RUNNING)));

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
	public void testStatus() throws CoreServiceException {
		salsa.getStatus(SERVICE_ID);
	}

	@Test
	public void testDeploymentDescription() throws CoreServiceException {
		salsa.getServiceDeploymentInfo(SERVICE_ID);
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

}
