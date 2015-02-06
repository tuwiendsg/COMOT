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
 * @author daniel-tuwien
 */
public class ProgrammingAndControllingElasticityWithCOMOTWithoutCassandra {

    public static void main(String[] args) {
        //specify service units in terms of software

        String salsaRepo = "http://128.130.172.215/salsa/upload/files/ElasticIoTNoDB/";

        //finally, we define Vm types for event processing
        OperatingSystemUnit loadbalancerVM = OperatingSystemUnit("LoadBalancerUnitVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("be6ae07b-7deb-4926-bfd7-b11afe228d6a")
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );

        OperatingSystemUnit eventProcessingVM = OperatingSystemUnit("EventProcessingUnitVM")
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
        ServiceUnit eventProcessingUnit = SingleSoftwareUnit("EventProcessingUnit")
                .deployedBy(SingleScriptArtifact("deployEventProcessingArtifact", salsaRepo + "deployEventProcessing.sh"))
                //event processing must register in Load Balancer, so it needs the IP
                .requires(Requirement.Variable("EventProcessingUnit_LoadBalancer_IP_Req"))
                //event processing also needs to querry the Data Controller to access data
                .provides(eventProcessingUnitScaleIn, eventProcessingUnitScaleOut)
                //scale IN if throughput < 200 and responseTime < 200
                .controlledBy(Strategy("EP_ST1")
                        .when(Constraint.MetricConstraint("EP_ST1_CO1", new Metric("responseTime", "ms")).lessThan("10"))
                        .and(Constraint.MetricConstraint("EP_ST1_CO2", new Metric("avgThroughput", "operations/s")).lessThan("200"))
                        .enforce(eventProcessingUnitScaleIn)
                )
                .withLifecycleAction(LifecyclePhase.STOP, BASHAction("sudo service event-processing stop"));

        //add the service units belonging to the event processing topology
        ServiceUnit loadbalancerUnit = SingleSoftwareUnit("LoadBalancerUnit")
                //load balancer must provide IP
                .exposes(Capability.Variable("LoadBalancer_IP_information"))
                .deployedBy(SingleScriptArtifact("deployLoadBalancerArtifact", salsaRepo + "deployLoadBalancer.sh"));

//        ServiceUnit mqttUnit = SingleSoftwareUnit("QueueUnit")
//                //load balancer must provide IP
//                .exposes(Capability.Variable("brokerIp_Capability"))
//                .deployedBy(SingleScriptArtifact("deployMQTTBroker", salsaRepo + "run_mqtt_broker.sh"));
//
//        ElasticityCapability localProcessingUnitScaleIn = ElasticityCapability.ScaleIn().withPrimitiveOperations("Salsa.scaleIn");
//        ElasticityCapability localProcessingUnitScaleOut = ElasticityCapability.ScaleOut().withPrimitiveOperations("Salsa.scaleOut");
//
//        ServiceUnit localProcessingUnit = SingleSoftwareUnit("LocalProcessingUnit")
//                //load balancer must provide IP
//                .requires(Requirement.Variable("brokerIp_Requirement"))
//                .requires(Requirement.Variable("loadBalancerIp_Requirement"))
//                .provides(localProcessingUnitScaleIn, localProcessingUnitScaleOut)
//                .deployedBy(SingleScriptArtifact("deployLocalProcessing", salsaRepo + "install-local-analysis-service.sh"));
        //define event processing unit topology
        ServiceTopology eventProcessingTopology = ServiceTopology("EventProcessingTopology")
                .withServiceUnits(loadbalancerUnit, eventProcessingUnit //add vm types to topology
                        , loadbalancerVM, eventProcessingVM
                );

//        ServiceTopology localProcessinTopology = ServiceTopology("Gateway")
//                .withServiceUnits(mqttQueueVM, mqttUnit, localProcessingUnit, localProcessingVM
//                );
//
//        localProcessingUnit.
//                controlledBy(Strategy("LPT_ST1").when(Constraint.MetricConstraint("LPT_ST1_CO1", new Metric("avgBufferSize", "#")).lessThan("50"))
//                        .enforce(localProcessingUnitScaleIn));
//
//        localProcessingUnit.constrainedBy(Constraint.MetricConstraint("LPT_CO1", new Metric("avgBufferSize", "#")).lessThan("200"));
        //TODO: de verificat de ce nu converteste ok daca pun si constraints si strategies pe topology
        eventProcessingTopology.constrainedBy(
                Constraint.MetricConstraint("EPT_CO1", new Metric("responseTime", "ms")).lessThan("20"));

//        localProcessingUnitScaleOut
//                .withCapabilityEffect(CapabilityEffect(localProcessingUnit)
//                        .withMetricEffect(
//                                MetricEffect().withMetric(new Metric("avgBufferSize", "#")).withType(MetricEffect.Type.SUB).withValue(200.0))
//                        .withMetricEffect(
//                                MetricEffect().withMetric(new Metric("bufferSize", "#")).withType(MetricEffect.Type.ADD).withValue(500.0)))
//                .withCapabilityEffect(CapabilityEffect(localProcessinTopology)
//                        .withMetricEffect(
//                                MetricEffect().withMetric(new Metric("avgBufferSize", "#")).withType(MetricEffect.Type.SUB).withValue(200.0))
//                        .withMetricEffect(
//                                MetricEffect().withMetric(new Metric("bufferSize", "#")).withType(MetricEffect.Type.ADD).withValue(500.0)));
//
//        localProcessingUnitScaleIn
//                .withCapabilityEffect(CapabilityEffect(localProcessingUnit)
//                        .withMetricEffect(
//                                MetricEffect().withMetric(new Metric("avgBufferSize", "#")).withType(MetricEffect.Type.ADD).withValue(90.0))
//                        .withMetricEffect(
//                                MetricEffect().withMetric(new Metric("bufferSize", "#")).withType(MetricEffect.Type.SUB).withValue(200.0)))
//                .withCapabilityEffect(CapabilityEffect(localProcessinTopology)
//                        .withMetricEffect(
//                                MetricEffect().withMetric(new Metric("avgBufferSize", "#")).withType(MetricEffect.Type.ADD).withValue(90.0))
//                        .withMetricEffect(
//                                MetricEffect().withMetric(new Metric("bufferSize", "#")).withType(MetricEffect.Type.SUB).withValue(200.0)));
        eventProcessingUnitScaleOut
                .withCapabilityEffect(CapabilityEffect(eventProcessingUnit)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cpuUsage", "#")).withType(MetricEffect.Type.SUB).withValue(40.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("responseTime", "ms")).withType(MetricEffect.Type.SUB).withValue(1000.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("throughput", "#")).withType(MetricEffect.Type.ADD).withValue(200.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cost", "$")).withType(MetricEffect.Type.ADD).withValue(0.12))
                )
                .withCapabilityEffect(CapabilityEffect(eventProcessingTopology)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cpuUsage", "#")).withType(MetricEffect.Type.SUB).withValue(20.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("responseTime", "ms")).withType(MetricEffect.Type.SUB).withValue(1000.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("throughput", "#")).withType(MetricEffect.Type.ADD).withValue(200.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cost", "$")).withType(MetricEffect.Type.ADD).withValue(0.12))
                );

        eventProcessingUnitScaleIn
                .withCapabilityEffect(CapabilityEffect(eventProcessingUnit)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cpuUsage", "#")).withType(MetricEffect.Type.ADD).withValue(40.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("responseTime", "ms")).withType(MetricEffect.Type.ADD).withValue(500.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("throughput", "#")).withType(MetricEffect.Type.SUB).withValue(100.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cost", "$")).withType(MetricEffect.Type.SUB).withValue(0.12))
                )
                .withCapabilityEffect(CapabilityEffect(eventProcessingTopology)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cpuUsage", "#")).withType(MetricEffect.Type.ADD).withValue(40.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("responseTime", "ms")).withType(MetricEffect.Type.ADD).withValue(500.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("throughput", "#")).withType(MetricEffect.Type.SUB).withValue(100.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cost", "$")).withType(MetricEffect.Type.SUB).withValue(0.12))
                );

        //describe the service template which will hold more topologies
        CloudService serviceTemplate = ServiceTemplate("ElasticIoTPlatformNoDB")
                .consistsOfTopologies(eventProcessingTopology)
                //                .consistsOfTopologies(localProcessinTopology)
                //defining CONNECT_TO and HOSTED_ON relationships
                .andRelationships(
                        //event processing gets IP from load balancer
                        ConnectToRelation("eventProcessingToLoadBalancer")
                        .from(loadbalancerUnit.getContext().get("LoadBalancer_IP_information"))
                        .to(eventProcessingUnit.getContext().get("EventProcessingUnit_LoadBalancer_IP_Req")) //specify which software unit goes to which VM
                        //                        ,
                        //                        ConnectToRelation("mqtt_broker")
                        //                        .from(mqttUnit.getContext().get("brokerIp_Capability"))
                        //                        .to(localProcessingUnit.getContext().get("brokerIp_Requirement")) //specify which software unit goes to which VM
                        //                        ,
                        //                        ConnectToRelation("load_balancer")
                        //                        .from(loadbalancerUnit.getContext().get("LoadBalancer_IP_information"))
                        //                        .to(localProcessingUnit.getContext().get("loadBalancerIp_Requirement")) //specify which software unit goes to which VM
                        , HostedOnRelation("loadbalancerToVM")
                        .from(loadbalancerUnit)
                        .to(loadbalancerVM),
                        HostedOnRelation("eventProcessingToVM")
                        .from(eventProcessingUnit)
                        .to(eventProcessingVM)
                //                        ,
                //                        HostedOnRelation("localProcessingToVM")
                //                        .from(localProcessingUnit)
                //                        .to(localProcessingVM),
                //                        HostedOnRelation("mqttToVM")
                //                        .from(mqttUnit)
                //                        .to(mqttQueueVM)
                )
                // as we have horizontally scalable distributed systems (one service unit can have more instances)
                //metrics must be aggregated among VMs
                .withDefaultMetrics();
        //to find scaling actions, one must assume some effects for each action, to understand
        //if it makes sense or not to execute the action
//                .withDefaultActionEffects();

        COMOTOrchestrator orchestrator = new COMOTOrchestrator("localhost").withSalsaPort(8380);

        orchestrator.controlExisting(serviceTemplate);
    }
}
