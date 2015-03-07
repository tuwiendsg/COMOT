package at.ac.tuwien.dsg.comot.orchestrator;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifact;
import static at.ac.tuwien.dsg.comot.common.model.BASHAction.BASHAction;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import static at.ac.tuwien.dsg.comot.common.model.CapabilityEffect.CapabilityEffect;
import static at.ac.tuwien.dsg.comot.common.model.MetricEffect.MetricEffect;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall;
import at.ac.tuwien.dsg.comot.common.model.Constraint;
import at.ac.tuwien.dsg.comot.common.model.Constraint.Metric;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.ConnectToRelation;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.Requirement;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import at.ac.tuwien.dsg.comot.common.model.ElasticityCapability;
import at.ac.tuwien.dsg.comot.common.model.LifecyclePhase;
import at.ac.tuwien.dsg.comot.common.model.MetricEffect;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.SingleSoftwareUnit;
import static at.ac.tuwien.dsg.comot.common.model.Strategy.Strategy;
import at.ac.tuwien.dsg.comot.orchestrator.interraction.COMOTOrchestrator;

/**
 *
 * @author http://dsg.tuwien.ac.at
 */
/**
 * Only Load Balancer and Event Processing
 *
 * @author daniel-tuwien
 */
public class MachineTask {

    public static void main(String[] args) {
        //specify service units in terms of software

        String salsaRepo = "http://128.130.172.215/salsa/upload/files/MachineTask/";

        //finally, we define Vm types for event processing
        OperatingSystemUnit loadbalancerVM = OperatingSystemUnit("LoadBalancerUnitVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("be6ae07b-7deb-4926-bfd7-b11afe228d6a")
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );

        OperatingSystemUnit eventProcessingVM = OperatingSystemUnit("MachineTaskUnitVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("be6ae07b-7deb-4926-bfd7-b11afe228d6a")
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );

//        OperatingSystemUnit localProcessingVM = OperatingSystemUnit("LocalProcessingUnitVM")
//                .providedBy(OpenstackSmall()
//                        .withBaseImage("be6ae07b-7deb-4926-bfd7-b11afe228d6a")
//                        .addSoftwarePackage("openjdk-7-jre")
//                        .addSoftwarePackage("ganglia-monitor")
//                        .addSoftwarePackage("gmetad")
//                ).andMinInstances(2);
//
//        OperatingSystemUnit mqttQueueVM = OperatingSystemUnit("MqttQueueVM")
//                .providedBy(OpenstackSmall()
//                        .withBaseImage("17ffd200-315f-4ba8-9e77-c294efc772bd")
//                        .addSoftwarePackage("openjdk-7-jre")
//                        .addSoftwarePackage("ganglia-monitor")
//                        .addSoftwarePackage("gmetad")
//                );
        ElasticityCapability eventProcessingUnitScaleIn = ElasticityCapability.ScaleIn();
        ElasticityCapability eventProcessingUnitScaleOut = ElasticityCapability.ScaleOut();

        //add the service units belonging to the event processing topology
        ServiceUnit eventProcessingUnit = SingleSoftwareUnit("MachineTaskUnit")
                .deployedBy(SingleScriptArtifact("deployEventProcessingArtifact", salsaRepo + "deployMachineTask.sh"))
                //event processing must register in Load Balancer, so it needs the IP
                .requires(Requirement.Variable("MachineTaskUnit_LoadBalancer_IP_Req"))
                //event processing also needs to querry the Data Controller to access data
                .provides(eventProcessingUnitScaleIn, eventProcessingUnitScaleOut)
                //scale IN if throughput < 200 and responseTime < 200
                //                .controlledBy(Strategy("EP_ST1")
                //                        .when(Constraint.MetricConstraint("EP_ST1_CO1", new Metric("totalProcessingTime", "ms")).lessThan("10"))
                //                        .and(Constraint.MetricConstraint("EP_ST1_CO2", new Metric("avgThroughput", "operations/s")).lessThan("200"))
                //                        .enforce(eventProcessingUnitScaleIn)
                //                )
                .withLifecycleAction(LifecyclePhase.STOP, BASHAction("sudo service machine-task-service stop"));

        //add the service units belonging to the event processing topology
        ServiceUnit loadbalancerUnit = SingleSoftwareUnit("LoadBalancerUnit")
                //load balancer must provide IP
                .exposes(Capability.Variable("LoadBalancer_IP_information"))
                .deployedBy(SingleScriptArtifact("deployLoadBalancerArtifact", salsaRepo + "deployLoadBalancer.sh"));

        ServiceTopology eventProcessingTopology = ServiceTopology("MachineTaskTopology")
                .withServiceUnits(loadbalancerUnit, eventProcessingUnit //add vm types to topology
                        , loadbalancerVM, eventProcessingVM
                );

        eventProcessingTopology.constrainedBy(
                Constraint.MetricConstraint("EPT_CO1", new Metric("responseTime", "ms")).lessThan("20"));

        CloudService serviceTemplate = ServiceTemplate("MachineTaskService")
                .consistsOfTopologies(eventProcessingTopology)
                //                .consistsOfTopologies(localProcessinTopology)
                //defining CONNECT_TO and HOSTED_ON relationships
                .andRelationships(
                        //event processing gets IP from load balancer
                        ConnectToRelation("machineTaskToLoadBalancer")
                        .from(loadbalancerUnit.getContext().get("LoadBalancer_IP_information"))
                        .to(eventProcessingUnit.getContext().get("MachineTaskUnit_LoadBalancer_IP_Req")) //specify which software unit goes to which VM
                        , HostedOnRelation("loadbalancerToVM")
                        .from(loadbalancerUnit)
                        .to(loadbalancerVM),
                        HostedOnRelation("eventProcessingToVM")
                        .from(eventProcessingUnit)
                        .to(eventProcessingVM)
                )
                .withDefaultMetrics();

        COMOTOrchestrator orchestrator = new COMOTOrchestrator("localhost").withSalsaPort(8080);

        orchestrator.deployAndControl(serviceTemplate);
    }
}
