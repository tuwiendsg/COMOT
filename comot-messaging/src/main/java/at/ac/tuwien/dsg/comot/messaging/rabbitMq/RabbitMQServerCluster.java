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
package at.ac.tuwien.dsg.comot.messaging.rabbitMq;

import at.ac.tuwien.dsg.cloud.utilities.messaging.api.ServerCluster;
import at.ac.tuwien.dsg.cloud.utilities.messaging.lightweight.util.ServerConfig;
import at.ac.tuwien.dsg.comot.client.DefaultSalsaClient;
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
import at.ac.tuwien.dsg.comot.orchestrator.interraction.salsa.SalsaInterraction;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentUnit;

/**
 * 
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class RabbitMQServerCluster implements ServerCluster {
	
	private String salsaRepo;
	private OperatingSystemUnit powerDnsServerVM;
	private OperatingSystemUnit rabbitServerVM;
	private ServiceUnit powerDnsServerUnit;
	private ServiceUnit rabbitServerUnit;
	private ServiceTopology powerDnsTopology;
	private ServiceTopology rabbitTopology;
	private CloudService service;
	private COMOTOrchestrator orchestrator;
	private ServerConfig config;
	private SalsaInterraction salsaInterraction;
	
	public RabbitMQServerCluster(ServerConfig config) {
		this.config = config;
		this.salsaRepo = String.format("http://%s/iCOMOTTutorial/files/comot-messaging", config.getSalsaIp());
		
		powerDnsServerVM = OperatingSystemUnit("PowerDnsVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("04a15006-b09e-461e-a992-efcb9f0f9c47")
                );
		
		rabbitServerVM = OperatingSystemUnit("RabbitMQServerVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("88be3072-5c89-473a-9d22-b72f2f818cff")
                );
		
		powerDnsServerUnit = SoftwareNode.SingleSoftwareUnit("PowerDnsUnit")
				.deployedBy(ArtifactTemplate.SingleScriptArtifact("deployPowerDnsServerArtifact", salsaRepo+"/deployPowerDnsServer.sh"))
				.exposes(Capability.Variable("PowerDnsIp"));
		
		rabbitServerUnit = SoftwareNode.SingleSoftwareUnit("RabbitServerUnit")
				.deployedBy(ArtifactTemplate.SingleScriptArtifact("deployRabbitMQServerArtifact", salsaRepo+"/deployRabbitMQServer.sh"))
				.requires(Requirement.Variable("PowerDnsIpReq").withName("PowerDnsIp"))
				.withMinInstances(config.getServerCount())
				.withMaxColocatedInstances(1);
		
		powerDnsTopology = ServiceTopology.ServiceTopology("PowerDnsServerTopology")
				.withServiceUnits(powerDnsServerUnit, powerDnsServerVM);
		
		rabbitTopology = ServiceTopology.ServiceTopology("RabbitServerTopology")
				.withServiceUnits(rabbitServerUnit, rabbitServerVM);
		
		service = CloudService.ServiceTemplate(this.config.getServiceName())
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
		
		orchestrator = new COMOTOrchestrator()
				.withIP(config.getSalsaIp())
                .withSalsaPort(config.getSalsaPort());
		
		salsaInterraction = new SalsaInterraction();
		DefaultSalsaClient client = new DefaultSalsaClient();
		client.getConfiguration().setHost(config.getSalsaIp());
		client.getConfiguration().setPort(config.getSalsaPort());
		salsaInterraction.withDefaultSalsaClient(client);
	}
	
	public void deploy() {
		
		if(isDeployed()) {
			this.changeServerCount(config.getServerCount());
			return;
		}
		
		this.orchestrator.deploy(this.service);
		salsaInterraction.waitUntilRunning(service.getId());
	}
	
	public boolean isDeployed() {
		DeploymentDescription desc = salsaInterraction.getServiceDeploymentInfo(service.getId());
		return desc.getDeployments().size()==0 ? false : true;
	}
	
	public int getServerCount() {
		DeploymentDescription desc = salsaInterraction.getServiceDeploymentInfo(service.getId());
		for(DeploymentUnit unit: desc.getDeployments()) {
			if(unit.getServiceUnitID().equals(this.rabbitServerUnit.getId())) {
				return unit.getAssociatedVMs().size();
			}
		}
		
		return 0;
	}
	
	public void changeServerCount(int count) {
		this.config.setServerCount(count);
		if(count <= 0) {
			salsaInterraction.undeploy(service.getId());
		}
		else {
			DeploymentDescription desc = salsaInterraction.getServiceDeploymentInfo(service.getId());
			for(DeploymentUnit d: desc.getDeployments()) {
				if(d.getServiceUnitID().equals(this.rabbitServerUnit.getId())) {
					
					int size = d.getAssociatedVMs().size();
					
					if(count > size) {
						this.salsaInterraction.spawn(service.getId(), rabbitTopology.getId(), rabbitServerUnit.getId(), (count-size));
					}
					
					while(count < size) {
						size--;
						salsaInterraction.destroy(service.getId(), rabbitTopology.getId(), rabbitServerVM.getId(), String.valueOf(size));
					}
				}
			}
		}
	}
	
}
