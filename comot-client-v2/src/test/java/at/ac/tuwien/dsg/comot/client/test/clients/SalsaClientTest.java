package at.ac.tuwien.dsg.comot.client.test.clients;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityType;
import at.ac.tuwien.dsg.comot.client.clients.SalsaClient;
import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.core.test.samples.ExampleDeployOneVM;

public class SalsaClientTest {

	private static final String SALSA_IP = "128.130.172.215";
	
	private SalsaClient salsa;
	private CloudService serviceTemplate;

	@Before
	public void setup() {
		salsa = new SalsaClient(SALSA_IP);
		
		serviceTemplate = ExampleDeployOneVM.build();
	}
	
	@After
	public void cleanUp(){
		salsa.close();
	}

	@Test
	public void testAutomated() throws CoreServiceException, InterruptedException {

		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService service;
		int countIter = 0;

		// deploy
		salsa.deploy(serviceTemplate);

		do {

			Thread.sleep(10000);
			service = salsa.getStatus(serviceTemplate.getId());

			if (countIter > 60) {
				countIter = 0;
				break;
			}
			
		} while (!(service.getState().equals(SalsaEntityState.DEPLOYED)
		|| service.getState().equals(SalsaEntityState.RUNNING)));

		// check DeploymentInfo
		assertNotNull(salsa.getServiceDeploymentInfo(serviceTemplate.getId()));

		int vm_count = service.getAllReplicaByType(SalsaEntityType.OPERATING_SYSTEM).size();
		assertEquals(1, vm_count);

		// spawn 2 new instances
		salsa.spawn(serviceTemplate.getId(), ExampleDeployOneVM.TOPOLOGY_ID, ExampleDeployOneVM.NODE_ID, 1);

		do {

			Thread.sleep(10000);
			service = salsa.getStatus(serviceTemplate.getId());

			if (countIter > 60) {
				countIter = 0;
				break;
			}

		} while (2 != service.getAllReplicaByType(SalsaEntityType.OPERATING_SYSTEM).size());

		vm_count = service.getAllReplicaByType(SalsaEntityType.OPERATING_SYSTEM).size();
		assertEquals(2, vm_count);

		// destroy one instance
		salsa.destroy(serviceTemplate.getId(), ExampleDeployOneVM.TOPOLOGY_ID, ExampleDeployOneVM.NODE_ID, 1);

		do {

			Thread.sleep(10000);
			service = salsa.getStatus(serviceTemplate.getId());

			if (countIter > 12) {
				countIter = 0;
				break;
			}

		} while (1 != service.getAllReplicaByType(SalsaEntityType.OPERATING_SYSTEM).size());

		vm_count = service.getAllReplicaByType(SalsaEntityType.OPERATING_SYSTEM).size();
		assertEquals(1, vm_count);

		// undeploy
		salsa.undeploy(serviceTemplate.getId());

		try {
			do {

				Thread.sleep(10000);
				service = salsa.getStatus(serviceTemplate.getId());

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
		salsa.deploy(serviceTemplate);
	}

	@Test
	public void testStatus() throws CoreServiceException {
		salsa.getStatus(serviceTemplate.getId());
	}

	@Test
	public void testDeploymentDescription() throws CoreServiceException {
		salsa.getServiceDeploymentInfo(serviceTemplate.getId());
	}

	@Test
	public void testUndeploy() throws CoreServiceException {
		salsa.undeploy(serviceTemplate.getId());
	}

	@Test
	public void testSpawnInstance() throws CoreServiceException {
		salsa.spawn(serviceTemplate.getId(), ExampleDeployOneVM.TOPOLOGY_ID, ExampleDeployOneVM.NODE_ID, 2);
	}

	@Test
	public void testDestroyInstance() throws CoreServiceException {
		salsa.destroy(serviceTemplate.getId(), ExampleDeployOneVM.TOPOLOGY_ID, ExampleDeployOneVM.NODE_ID, 1);
	}

}
