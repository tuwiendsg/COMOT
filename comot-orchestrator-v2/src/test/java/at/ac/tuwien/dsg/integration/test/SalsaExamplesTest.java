package at.ac.tuwien.dsg.integration.test;

import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackMicro;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;

import org.junit.Test;

import at.ac.tuwien.dsg.comot.client.DefaultSalsaClient;
import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import at.ac.tuwien.dsg.comot.orchestrator.ComotOrchestrator;
import at.ac.tuwien.dsg.comot.orchestrator.SalsaInterraction;

/**
 * https://github.com/tuwiendsg/SALSA/tree/master/examples
 * 
 * @author jurajcik
 *
 */
public class SalsaExamplesTest {

	private static final String SALSA_IP = "128.130.172.215";
	
	@Test
	public void testDeployOneVM() throws CoreServiceException{
		
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
	public void testDeployExecutableOnVM(){
		
	}
	
	
}
