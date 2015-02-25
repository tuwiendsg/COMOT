/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.orchestrator;

import at.ac.tuwien.dsg.comot.api.ToscaDescriptionBuilder;
import at.ac.tuwien.dsg.comot.api.ToscaDescriptionBuilderImpl;
import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.DockerFileArtifact;
import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.MiscArtifact;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.DockerDefault;
import at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall;
import static at.ac.tuwien.dsg.comot.common.model.DockerUnit.DockerUnit;
import at.ac.tuwien.dsg.comot.common.model.DockerUnit;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import at.ac.tuwien.dsg.comot.orchestrator.interraction.COMOTOrchestrator;

/**
 *
 * @author hungld
 */
public class DeployIoTComponent {
     public static void main(String[] args) {
         OperatingSystemUnit gatewayVM = OperatingSystemUnit("gatewayVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("7ac2cc53-2301-40d7-a030-910d72f552ff")  // this image include docker, faster spin up
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );
         
         DockerUnit gatewayDocker = DockerUnit("gatewayDocker")
                 .providedBy(DockerDefault())
                .deployedBy(DockerFileArtifact("dockerFileArtifact","http://128.130.172.215/salsa/upload/files/rtGovOps/Dockerfile"),
                            MiscArtifact("starter.sh", "http://128.130.172.215/salsa/upload/files/rtGovOps/starter.sh"),
                            MiscArtifact("achieveArtifact", "http://128.130.172.215/salsa/upload/files/rtGovOps/rtGovOps-agent.tar.gz"));
         
         ServiceTopology gatewayTopology = ServiceTopology("gatewayTopology")
                .withServiceUnits(gatewayVM, gatewayDocker);
         
         CloudService serviceTemplate = ServiceTemplate("GatewayDockerService")
                .consistsOfTopologies(gatewayTopology)               
                //defining CONNECT_TO and HOSTED_ON relationships
                .andRelationships(
                        HostedOnRelation("dockerOnVM")
                        .from(gatewayDocker)
                        .to(gatewayVM)
                );
         
         ToscaDescriptionBuilder toscaBuilder= new ToscaDescriptionBuilderImpl();
         String tosca = toscaBuilder.toXml(serviceTemplate);
         System.out.println(tosca);
         
         COMOTOrchestrator orchestrator = new COMOTOrchestrator()
                //we have SALSA as cloud management tool
                //curently deployed separately
                .withSalsaIP("localhost")
                .withSalsaPort(8380)
 
                //ifwe have rSYBL elasticity control service and MELA 
                //deployed separately
                .withRsyblIP("localhost")
                .withRsyblPort(8280);
 
        //orchestrator.deploy(serviceTemplate);
     }
}


