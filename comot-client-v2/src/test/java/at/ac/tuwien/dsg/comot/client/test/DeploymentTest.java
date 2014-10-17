package at.ac.tuwien.dsg.comot.client.test;

import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackMicro;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;

public class DeploymentTest extends AbstractTest{

	@Autowired
	private DeploymentClient deploymentClient;
	
	private static final String serviceId = "example_deployOneVM";
	private static final String topologyId = "example_topology";
	private static final String nodeId = "example_OS";

	
	@Test
	public void aaa() throws CoreServiceException{
		
		OperatingSystemUnit dataControllerVM = OperatingSystemUnit(nodeId)
				.providedBy(OpenstackMicro("example_VM")
						.addSoftwarePackage("openjdk-7-jre"))
				.andMaxInstances(5);

		ServiceTopology topology = ServiceTopology(topologyId)
				.withServiceUnits(dataControllerVM);

		CloudService serviceTemplate = ServiceTemplate(serviceId)
				.consistsOfTopologies(topology)
				.withDefaultMetrics()
				.withDefaultActionEffects();
		
		deploymentClient.deploy(serviceTemplate);
	}
}
