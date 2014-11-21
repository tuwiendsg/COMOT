package at.ac.tuwien.dsg.comot.orchestrator;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifactTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CapabilityEffect.CapabilityEffect;
import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.DockerDefault;
import static at.ac.tuwien.dsg.comot.common.model.DockerUnit.DockerUnit;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.ConnectToRelation;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import static at.ac.tuwien.dsg.comot.common.model.MetricEffect.MetricEffect;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.SingleSoftwareUnit;
import static at.ac.tuwien.dsg.comot.common.model.Strategy.Strategy;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.common.model.Constraint;
import at.ac.tuwien.dsg.comot.common.model.Constraint.Metric;
import at.ac.tuwien.dsg.comot.common.model.DockerUnit;
import at.ac.tuwien.dsg.comot.common.model.ElasticityCapability;
import at.ac.tuwien.dsg.comot.common.model.MetricEffect;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall;
import at.ac.tuwien.dsg.comot.common.model.Requirement;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.Strategy;
import at.ac.tuwien.dsg.orchestrator.interraction.COMOTOrchestrator;

/**
 *
 * @author http://dsg.tuwien.ac.at
 */
public class ComotHelloElasticity {

    public static void main(String[] args) {
        //specify service units in terms of software

        String salsaRepo = "http://128.130.172.215/repository/files/HelloElasticity/";
         

        //then, we define Docker types for event processing
        DockerUnit loadbalancerDocker = DockerUnit("LoadBalancerUnitDocker")
                .providedBy(DockerDefault("DockerDefault_LB")
        		.addSoftwarePackage("ganglia-monitor")
                .addSoftwarePackage("gmetad")
                ).withMaxInstances(Integer.MAX_VALUE);

        DockerUnit eventProcessingDocker = DockerUnit("EventProcessingUnitDocker")
                .providedBy(DockerDefault("DockerDefault_EP")
        		.addSoftwarePackage("ganglia-monitor")
                .addSoftwarePackage("gmetad")
                ).withMaxInstances(Integer.MAX_VALUE);
        
        //finally, we define Vm types for docker of event processing
        OperatingSystemUnit eventProcessingVM = OperatingSystemUnit("EventProcessingUnitVM")
                .providedBy(OpenstackSmall("OpenStackMicro_OS_EP")
                        .withBaseImage("7ac2cc53-2301-40d7-a030-910d72f552ff")
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
                ).withMinInstances(2)
                .withMaxInstances(1);

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
                		, loadbalancerDocker, eventProcessingDocker
                        , eventProcessingVM
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
                        	.to(eventProcessingUnit.getContext().get("EventProcessingUnit_LoadBalancer_IP_Req")), //specify which software unit goes to which VM
                        HostedOnRelation("loadbalancerToDocker")
	                        .from(loadbalancerUnit)
	                        .to(loadbalancerDocker),
                        HostedOnRelation("loadbalancerDockerToVM")
                        	.from(loadbalancerDocker)
                        	.to(eventProcessingVM),
                        HostedOnRelation("eventProcessingToDocker")
                        	.from(eventProcessingUnit)
                        	.to(eventProcessingDocker),
                        HostedOnRelation("eventProcessingDockerToVM")
                        	.from(eventProcessingDocker)
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
