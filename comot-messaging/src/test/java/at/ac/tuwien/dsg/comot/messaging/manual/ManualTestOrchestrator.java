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
package at.ac.tuwien.dsg.comot.messaging.manual;

import at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall;
import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;
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
public class ManualTestOrchestrator {
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		String salsaRepo = "http://128.130.172.215/iCOMOTTutorial/files/comot-messaging";
		
		OperatingSystemUnit producerVM = OperatingSystemUnit.OperatingSystemUnit("producerVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("ed4fee32-bd73-482a-a725-98c24b273899")
                );
		
		OperatingSystemUnit consumerVM = OperatingSystemUnit.OperatingSystemUnit("consumerVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("ed4fee32-bd73-482a-a725-98c24b273899")
                );
		
		ServiceUnit producerUnit = SoftwareNode.SingleSoftwareUnit("ProducerUnit")
				.deployedBy(ArtifactTemplate.SingleScriptArtifact("deployProducerArtifact", salsaRepo+"/deployManualTest.sh"));
		
		ServiceUnit consumerUnit = SoftwareNode.SingleSoftwareUnit("ConsumerUnit")
				.deployedBy(ArtifactTemplate.SingleScriptArtifact("deployConsumerArtifact", salsaRepo+"/deployManualTest.sh"));
		
		ServiceTopology producerTopology = ServiceTopology.ServiceTopology("ProducerTopology")
				.withServiceUnits(producerUnit, producerVM);
		
		ServiceTopology consumerTopology = ServiceTopology.ServiceTopology("ConsumerTopology")
				.withServiceUnits(consumerUnit, consumerVM);
		
		CloudService service = CloudService.ServiceTemplate("ComotMessagingManualTest")
				.consistsOfTopologies(producerTopology)
				.consistsOfTopologies(consumerTopology)
				.andRelationships(
						EntityRelationship.HostedOnRelation("producerToVM")
						.from(producerUnit)
						.to(producerVM),
						EntityRelationship.HostedOnRelation("consumerToVM")
						.from(consumerUnit)
						.to(consumerVM)
				)
				.withDefaultMetrics();
		
		COMOTOrchestrator orchestrator = new COMOTOrchestrator()
				.withIP("128.130.172.215")
                .withSalsaPort(8080);
		
		orchestrator.deploy(service);
	}
}
