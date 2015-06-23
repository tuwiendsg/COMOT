/* 
 * Copyright 2015 Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.ac.tuwien.dsg.comot.orchestrator;

import at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall;
import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.Requirement;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.SoftwareNode;
import at.ac.tuwien.dsg.comot.orchestrator.interraction.COMOTOrchestrator;

/**
 * 
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class RabbitMQServer {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
        String salsaRepo = "http://128.130.172.215/repository/files/comot-messaging";

		OperatingSystemUnit powerDnsServerVM = OperatingSystemUnit("PowerDnsVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("a82e054f-4f01-49f9-bc4c-77a98045739c")
                );
		
        //need to specify details of VM and operating system to deploy the software servide units on
        OperatingSystemUnit rabbitServerVM = OperatingSystemUnit("RabbitMQServerVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("a82e054f-4f01-49f9-bc4c-77a98045739c")
                );
		
		ServiceUnit powerDnsServerUnit = SoftwareNode.SingleSoftwareUnit("PowerDnsUnit")
				.deployedBy(ArtifactTemplate.SingleScriptArtifact("deployPowerDnsServerArtifact", salsaRepo+"/deployPowerDnsServer.sh"))
				.exposes(Capability.Variable("PowerDnsIp"));
		
		ServiceUnit rabbitServerUnit = SoftwareNode.SingleSoftwareUnit("RabbitServerUnit")
				.deployedBy(ArtifactTemplate.SingleScriptArtifact("deployRabbitMQServerArtifact", salsaRepo+"/deployRabbitMQServer.sh"))
				.requires(Requirement.Variable("PowerDnsIpReq"));
		
		ServiceTopology powerDnsTopology = ServiceTopology.ServiceTopology("PowerDnsServerTopology")
				.withServiceUnits(powerDnsServerUnit, powerDnsServerVM);
		
		ServiceTopology rabbitTopology = ServiceTopology.ServiceTopology("RabbitServerTopology")
				.withServiceUnits(rabbitServerUnit, rabbitServerVM);
		
		/*CloudService powerDnsService = CloudService.ServiceTemplate("PowerDnsServerService")
				.consistsOfTopologies(powerDnsTopology)
				.andRelationships(
						EntityRelationship.HostedOnRelation("powerDnsServerToVM")
						.from(powerDnsServerUnit)
						.to(powerDnsServerVM)
				)
				
				.withDefaultMetrics();
		
		CloudService rabbitService = CloudService.ServiceTemplate("RabbitServerService")
				.consistsOfTopologies(rabbitTopology)
				.andRelationships(
						EntityRelationship.HostedOnRelation("rabbitServerToVM")
						.from(rabbitServerUnit)
						.to(rabbitServerVM)
				)
				.withDefaultMetrics();*/
		
		CloudService service = CloudService.ServiceTemplate("RabbitServerService")
				.consistsOfTopologies(rabbitTopology)
				.consistsOfTopologies(powerDnsTopology)
				.andRelationships(
						EntityRelationship.ConnectToRelation("rabbitServerToPowerDns")
						.from(powerDnsServerUnit.getContext().get("PowerDnsIp"))
						.to(rabbitServerUnit.getContext().get("PowerDnsIpReq")),
						EntityRelationship.HostedOnRelation("powerDnsServerToVM")
						.from(powerDnsServerUnit)
						.to(powerDnsServerVM),
						EntityRelationship.HostedOnRelation("rabbitServerToVM")
						.from(rabbitServerUnit)
						.to(rabbitServerVM)
				)
				.withDefaultMetrics();
		
		COMOTOrchestrator orchestrator = new COMOTOrchestrator()
				.withIP("128.130.172.215")
                .withSalsaPort(8380);
		
		orchestrator.deploy(service);
	}
	
}
