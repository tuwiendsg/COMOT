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
import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifact;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.DockerDefault;
import at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall;
import static at.ac.tuwien.dsg.comot.common.model.DockerUnit.DockerUnit;
import at.ac.tuwien.dsg.comot.common.model.DockerUnit;
import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.ConnectToRelation;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.Requirement;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.SoftwareNode;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.SingleSoftwareUnit;
import at.ac.tuwien.dsg.comot.orchestrator.interraction.COMOTOrchestrator;

/**
 *
 * @author hungld
 */
public class DeployIoTComponent {

    public static void main(String[] args) {

        String salsaRepo = "http://128.130.172.215/salsa/upload/files/rtGovOps/";

        ServiceUnit MqttQueueVM = OperatingSystemUnit("MqttQueueVM")
                .providedBy(OpenstackSmall())
                .andReference("ElasticIoTPlatform/MqttQueueVM");

        ServiceUnit QueueUnit = SoftwareNode.SingleSoftwareUnit("QueueUnit")
                .exposes(Capability.Variable("brokerIp_Capability"))
                .andReference("ElasticIoTPlatform/QueueUnit");

        OperatingSystemUnit gatewayVM = OperatingSystemUnit("gatewayVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("7ac2cc53-2301-40d7-a030-910d72f552ff") // this image include docker, faster spin up
                //                        .addSoftwarePackage("ganglia-monitor")
                //                        .addSoftwarePackage("gmetad")
                ).withMaxColocatedInstances(1);

        DockerUnit gatewayDocker = DockerUnit("gatewayDocker")
                .providedBy(DockerDefault())
                .deployedBy(DockerFileArtifact("dockerFileArtifact", salsaRepo + "Dockerfile-UB"),
                        MiscArtifact("starter.sh", salsaRepo + "starter_ubuntu.sh"),
                        MiscArtifact("achieveArtifact", salsaRepo + "rtGovOps-agents.tar.gz"))
                .withMaxColocatedInstances(1);

        ServiceUnit sensorUnit = SingleSoftwareUnit("sensorUnit")
                .requires(Requirement.Variable("brokerIp_Requirement"))
                .deployedBy(SingleScriptArtifact("deploySensorUnit", salsaRepo + "deploySensorUnit.sh"))
                .withMaxColocatedInstances(1);
//                , MiscArtifact("sensor.tar.gz", salsaRepo + "sensor.tar.gz"));

        ServiceTopology gatewayTopology = ServiceTopology("IoTTopology")
                .withServiceUnits(sensorUnit, gatewayVM, gatewayDocker)
                .withServiceUnits(MqttQueueVM, QueueUnit);

        CloudService serviceTemplate = ServiceTemplate("IoTSensors")
                //         CloudService serviceTemplate = ServiceTemplate("GatewayDockerService")
                .consistsOfTopologies(gatewayTopology)
                //defining CONNECT_TO and HOSTED_ON relationships
                .andRelationships(HostedOnRelation("dockerOnVM")
                        .from(gatewayDocker)
                        .to(gatewayVM))
                .andRelationships(HostedOnRelation("SensorOnDocker")
                        .from(sensorUnit)
                        .to(gatewayDocker))
                .andRelationships(HostedOnRelation("QueueUnitOnMqttQueueVM")
                        .from(QueueUnit)
                        .to(MqttQueueVM))
                // note: the ID of connectto relationship for Sensors must be started with "mqtt", the sensor code is hard-coded to read this pattern.
                .andRelationships(ConnectToRelation("mqtt-broker")
                        .from(QueueUnit.getContext().get("brokerIp_Capability"))
                        .to(sensorUnit.getContext().get("brokerIp_Requirement")));

        ToscaDescriptionBuilder toscaBuilder = new ToscaDescriptionBuilderImpl();
        String tosca = toscaBuilder.toXml(serviceTemplate);
        System.out.println(tosca);

        COMOTOrchestrator orchestrator = new COMOTOrchestrator()
                //we have SALSA as cloud management tool
                //curently deployed separately
                .withSalsaIP("128.130.172.215")
                .withSalsaPort(8380)
                //ifwe have rSYBL elasticity control service and MELA 
                //deployed separately
                .withRsyblIP("128.130.172.215")
                .withRsyblPort(8280);

        orchestrator.deploy(serviceTemplate);
    }
}
