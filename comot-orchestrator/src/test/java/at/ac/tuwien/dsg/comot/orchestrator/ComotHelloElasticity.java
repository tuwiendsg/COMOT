package at.ac.tuwien.dsg.comot.orchestrator;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import static at.ac.tuwien.dsg.comot.common.model.CapabilityEffect.CapabilityEffect;
import static at.ac.tuwien.dsg.comot.common.model.MetricEffect.MetricEffect;
import at.ac.tuwien.dsg.comot.common.model.Constraint;
import at.ac.tuwien.dsg.comot.common.model.Constraint.Metric;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.ConnectToRelation;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import at.ac.tuwien.dsg.comot.common.model.Requirement;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.DockerDefault;
import at.ac.tuwien.dsg.comot.common.model.DockerUnit;
import static at.ac.tuwien.dsg.comot.common.model.DockerUnit.DockerUnit;
import at.ac.tuwien.dsg.comot.common.model.ElasticityCapability;
import at.ac.tuwien.dsg.comot.common.model.MetricEffect;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.SingleSoftwareUnit;
import at.ac.tuwien.dsg.comot.common.model.Strategy;
import static at.ac.tuwien.dsg.comot.common.model.Strategy.Strategy;
import at.ac.tuwien.dsg.orchestrator.interraction.COMOTOrchestrator;

/**
 *
 * @author http://dsg.tuwien.ac.at
 */
public class ComotHelloElasticity {

    public static void main(String[] args) {
        //specify service units in terms of software

        String salsaRepo = "http://128.130.172.215/repository/files/HelloElasticity/";
         

        //finally, we define Vm types for event processing
        DockerUnit loadbalancerVM = DockerUnit("LoadBalancerUnitVM")
                .providedBy(DockerDefault("OpenStackSmall_OS_LB")
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );

        DockerUnit eventProcessingVM = DockerUnit("EventProcessingUnitVM")
                .providedBy(DockerDefault("OpenStackMicro_OS_EP")
                        .withBaseImage("be6ae07b-7deb-4926-bfd7-b11afe228d6a")
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );

        //add the service units belonging to the event processing topology
        ServiceUnit eventProcessingUnit = SingleSoftwareUnit("EventProcessingUnit")
                .deployedBy(SingleScriptArtifactTemplate("deployEventProcessingArtifact", salsaRepo + "deployEventProcessing.sh"))
                //                .andMinInstances(2)
                //event processing must register in Load Balancer, so it needs the IP
                .requires(Requirement.Variable("EventProcessingUnit_LoadBalancer_IP_Req"))
                //event processing also needs to querry the Data Controller to access data
                //scale IN if throughput < 200 and responseTime < 200
                .controlledBy(
                        Strategy("EP_ST1")
                        .when(Constraint.MetricConstraint("EP_ST1_CO1", new Metric("responseTime", "ms")).lessThan("1000"))
                        .and(Constraint.MetricConstraint("EP_ST1_CO2", new Metric("totalPendingRequests", "#")).lessThan("10"))
                        .then(Strategy.Action.ScaleIn),
                        Strategy("EP_ST2")
                        .when(Constraint.MetricConstraint("EP_ST1_CO1", new Metric("responseTime", "ms")).greaterThan("1000"))
                        .and(Constraint.MetricConstraint("EP_ST1_CO2", new Metric("totalPendingRequests", "#")).greaterThan("10"))
                        .then(Strategy.Action.ScaleOut)
                ).withMinInstances(2);

        //add the service units belonging to the event processing topology
        ServiceUnit loadbalancerUnit = SingleSoftwareUnit("LoadBalancerUnit")
                //load balancer must provide IP
                .exposes(Capability.Variable("LoadBalancer_IP_information"))
                .deployedBy(SingleScriptArtifactTemplate("deployLoadBalancerArtifact", salsaRepo + "deployLoadBalancer.sh"));

        ServiceUnit mqttUnit = SingleSoftwareUnit("QueueUnit")
                //load balancer must provide IP
                .exposes(Capability.Variable("brokerIp_Capability"))
                .deployedBy(SingleScriptArtifactTemplate("deployMQTTBroker", salsaRepo + "run_mqtt_broker.sh"));

        //define event processing unit topology
        ServiceTopology eventProcessingTopology = ServiceTopology("EventProcessingTopology")
                .withServiceUnits(loadbalancerUnit, eventProcessingUnit //add vm types to topology
                        , loadbalancerVM, eventProcessingVM
                );

        //TODO: de verificat de ce nu converteste ok daca pun si constraints si strategies pe topology
        eventProcessingUnit.constrainedBy(
                Constraint.MetricConstraint("EPT_CO1", new Metric("responseTime", "ms")).lessThan("1000"),
                Constraint.MetricConstraint("EPT_CO2", new Metric("totalPendingRequests", "#")).lessThan("10"));

        eventProcessingUnit.provides(
                ElasticityCapability.ScaleOut("ScaleOutEventProcessing").withPrimitiveOperations("Salsa.scaleOut")
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
                )
        );
        eventProcessingUnit.provides(
                ElasticityCapability.ScaleIn("ScaleInEventProcessing").withPrimitiveOperations("M2MDaaS.decommissionWS", "Salsa.scaleIn")
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
                )
        );
        //describe the service template which will hold more topologies
        CloudService serviceTemplate = ServiceTemplate("HelloElasticity")
                .consistsOfTopologies(eventProcessingTopology)
                //defining CONNECT_TO and HOSTED_ON relationships
                .andRelationships(
                        //event processing gets IP from load balancer
                        ConnectToRelation("eventProcessingToLoadBalancer")
                        .from(loadbalancerUnit.getContext().get("LoadBalancer_IP_information"))
                        .to(eventProcessingUnit.getContext().get("EventProcessingUnit_LoadBalancer_IP_Req")) //specify which software unit goes to which VM
                        , HostedOnRelation("loadbalancerToVM")
                        .from(loadbalancerUnit)
                        .to(loadbalancerVM),
                        HostedOnRelation("eventProcessingToVM")
                        .from(eventProcessingUnit)
                        .to(eventProcessingVM)
                )
                // as we have horizontally scalable distributed systems (one service unit can have more instances)
                //metrics must be aggregated among VMs
                .withDefaultMetrics();
                //to find scaling actions, one must assume some effects for each action, to understand
        //if it makes sense or not to execute the action
//                .withDefaultActionEffects();

        //instantiate COMOT orchestrator to deploy, monitor and control the service
        COMOTOrchestrator orchestrator = new COMOTOrchestrator()
                //we have SALSA as cloud management tool
                //curently deployed separately
                .withSalsaIP("128.130.172.215")
                .withSalsaPort(8080)
                //we have rSYBL elasticity control service and MELA 
                //deployed separately
                //                .withRsyblIP("109.231.121.26")
                //                .withRsyblIP("localhost")
                //                                .withRsyblIP("109.231.121.104")
                .withRsyblIP("128.130.172.214")
                //                .withRsyblIP("128.131.172.4118")
                .withRsyblPort(8280);
//                .withRsyblPort(8080);

        //deploy, monitor and control
//        orchestrator.deployAndControl(serviceTemplate);
        orchestrator.deploy(serviceTemplate);
//        orchestrator.controlExisting(serviceTemplate);

    }
}
