package at.ac.tuwien.dsg.integration.test;

import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackMicro;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;

import org.junit.Test;

import at.ac.tuwien.dsg.comot.client.DefaultSalsaClient;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import at.ac.tuwien.dsg.integration.interraction.ComotOrchestrator;
import at.ac.tuwien.dsg.integration.interraction.salsa.SalsaInterraction;

public class BasicTest {

	private static final String SALSA_IP = "128.130.172.215";
	
	@Test
	public void testBasic(){
		
		 OperatingSystemUnit dataControllerVM = OperatingSystemUnit("example_OS")
	                .providedBy(OpenstackMicro("example_VM")
	                        .addSoftwarePackage("openjdk-7-jre")
	                );
		 
		 ServiceTopology topology = ServiceTopology("example_topology")
	                .withServiceUnits(dataControllerVM);
		
		 CloudService serviceTemplate = ServiceTemplate("example_deployOneVM")
				 .consistsOfTopologies(topology)
				 .withDefaultMetrics()
				 .withDefaultActionEffects();
		 
		 ComotOrchestrator orchestrator = new ComotOrchestrator()
		         .withSalsaIP(SALSA_IP)
		         .withSalsaPort(8080)
		         .withRsyblIP("localhost")
		         .withRsyblPort(8280);
		 
		 orchestrator.deploy(serviceTemplate);

	}
	
	@Test
	public void testLogging(){
		DefaultSalsaClient defaultSalsaClient = new DefaultSalsaClient();
		defaultSalsaClient.getConfiguration().setHost(SALSA_IP);
		SalsaInterraction salsaInterraction = new SalsaInterraction().withDefaultSalsaClient(defaultSalsaClient);
	
		salsaInterraction.waitUntilRunning("example_deployOneVM");
	}
}
