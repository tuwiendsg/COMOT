package at.ac.tuwien.dsg.comot.client.test.stub;

import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackMicro;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.client.ControlClientRsybl;
import at.ac.tuwien.dsg.comot.client.stub.RsyblStub;
import at.ac.tuwien.dsg.comot.client.stub.SalsaStub;
import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

public class RsyblStubTest {

	private static final Logger log = LoggerFactory.getLogger(RsyblStubTest.class);

	private static final String SALSA_IP = "128.130.172.215";
	
	private static final String serviceId = "example_deployOneVM";
	private static final String topologyId = "example_topology";
	private static final String nodeId = "example_OS";

	private SalsaStub salsa;
	private RsyblStub rsybl;
	private ControlClientRsybl rsyblService;
	private CloudService serviceTemplate;

	@Before
	public void setup() {
		rsybl = new RsyblStub("localhost", 8280);
		//rsyblService = new ControlClientRsybl(rsybl);
		
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
	
	@After
	public void cleanUp(){
		rsybl.close();
	}
	
	@Test
	public void testServiceDeployment() throws CoreServiceException {
		
		//DeploymentDescription depDesc =salsa.getServiceDeploymentInfo(serviceId);
		
		//rsyblService.sendInitialConfig(serviceTemplate, depDesc, null, null);
	}
}