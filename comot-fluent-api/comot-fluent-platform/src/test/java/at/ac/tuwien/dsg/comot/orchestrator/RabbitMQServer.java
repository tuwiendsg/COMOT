/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.orchestrator;

import at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall;
import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.SoftwareNode;
import at.ac.tuwien.dsg.comot.orchestrator.interraction.COMOTOrchestrator;

/**
 *
 * @author vauvenal5
 */
public class RabbitMQServer {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
        String salsaRepo = "http://128.130.172.215/salsa/upload/files/TMP/comot-messaging/";

        //need to specify details of VM and operating system to deploy the software servide units on
        OperatingSystemUnit rabbitServerVM = OperatingSystemUnit("RabbitMQServerVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("a82e054f-4f01-49f9-bc4c-77a98045739c")
                );
		
		ServiceUnit rabbitServerUnit = SoftwareNode.SingleSoftwareUnit("RabbitServerUnit")
				.deployedBy(ArtifactTemplate.SingleScriptArtifact("deployRabbitMQServerArtifact", salsaRepo+"deployRabbitMQServer.sh"));
		
		ServiceTopology rabbitTopology = ServiceTopology.ServiceTopology("RabbitServerTopology")
				.withServiceUnits(rabbitServerUnit, rabbitServerVM);
		
		CloudService rabbitService = CloudService.ServiceTemplate("RabbitServerService")
				.consistsOfTopologies(rabbitTopology)
				.andRelationships(
						EntityRelationship.HostedOnRelation("rabbitServerToVM")
						.from(rabbitServerUnit)
						.to(rabbitServerVM)
				)
				.withDefaultMetrics();
		
		COMOTOrchestrator orchestrator = new COMOTOrchestrator()
				.withIP("128.130.172.215")
                .withSalsaPort(8380);
		
		orchestrator.deploy(rabbitService);
	}
	
}
