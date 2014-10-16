package at.ac.tuwien.dsg.comot.client.test;

import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackMicro;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityType;
import at.ac.tuwien.dsg.comot.client.SalsaStub;
import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;

public class SalsaStubTest {

	private static final Logger log = LoggerFactory.getLogger(SalsaStubTest.class);

	private static final String SALSA_IP = "128.130.172.215";
	private static final String serviceId = "example_deployOneVM";
	private static final String topologyId = "example_topology";
	private static final String nodeId = "example_OS";

	private SalsaStub salsa;
	private CloudService serviceTemplate;

	@Before
	public void setup() {
		salsa = new SalsaStub(SALSA_IP);

		OperatingSystemUnit dataControllerVM = OperatingSystemUnit(nodeId)
				.providedBy(OpenstackMicro("example_VM")
						.addSoftwarePackage("openjdk-7-jre"))
				.andMaxInstances(5);

		ServiceTopology topology = ServiceTopology(topologyId)
				.withServiceUnits(dataControllerVM);

		serviceTemplate = ServiceTemplate(serviceId)
				.consistsOfTopologies(topology)
				.withDefaultMetrics()
				.withDefaultActionEffects();
	}

	@Test
	public void testAutomated() throws CoreServiceException, InterruptedException {

		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService service;
		int countIter = 0;

		// deploy
		salsa.deploy(serviceTemplate);

		do {

			Thread.sleep(10000);
			service = salsa.getStatus(serviceId);

			if (countIter > 60) {
				countIter = 0;
				break;
			}
			
		} while (!(service.getState().equals(SalsaEntityState.DEPLOYED)
		|| service.getState().equals(SalsaEntityState.RUNNING)));

		// check DeploymentInfo
		assertNotNull(salsa.getServiceDeploymentInfo(serviceId));

		int vm_count = service.getAllReplicaByType(SalsaEntityType.OPERATING_SYSTEM).size();
		assertEquals(1, vm_count);

		// spawn 2 new instances
		salsa.spawn(serviceId, topologyId, nodeId, 2);

		do {

			Thread.sleep(10000);
			service = salsa.getStatus(serviceId);

			if (countIter > 12) {
				countIter = 0;
				break;
			}

		} while (3 != service.getAllReplicaByType(SalsaEntityType.OPERATING_SYSTEM).size());

		assertEquals(3, vm_count);

		// destroy one instance
		salsa.destroy(serviceId, topologyId, nodeId, 1);

		do {

			Thread.sleep(10000);
			service = salsa.getStatus(serviceId);

			if (countIter > 12) {
				countIter = 0;
				break;
			}

		} while (2 != service.getAllReplicaByType(SalsaEntityType.OPERATING_SYSTEM).size());

		assertEquals(2, vm_count);

		// undeploy
		salsa.undeploy(serviceId);

		try {
			do {

				Thread.sleep(10000);
				service = salsa.getStatus(serviceId);

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
		salsa.getStatus(serviceId);
	}

	@Test
	public void testDeploymentDescription() throws CoreServiceException {
		salsa.getServiceDeploymentInfo(serviceId);
	}

	@Test
	public void testUndeploy() throws CoreServiceException {
		salsa.undeploy(serviceId);
	}

	@Test
	public void testSpawnInstance() throws CoreServiceException {
		salsa.spawn(serviceId, topologyId, nodeId, 2);
	}

	@Test
	public void testDestroyInstance() throws CoreServiceException {
		salsa.destroy(serviceId, topologyId, nodeId, 1);
	}

}
