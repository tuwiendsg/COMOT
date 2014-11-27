package at.ac.tuwien.dsg.comot.orchestrator;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import at.ac.tuwien.dsg.comot.common.model.CapabilityEffect;
import static at.ac.tuwien.dsg.comot.common.model.CapabilityEffect.CapabilityEffect;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import static at.ac.tuwien.dsg.comot.common.model.MetricEffect.MetricEffect;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackMicro;
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
import at.ac.tuwien.dsg.comot.common.model.MetricEffect;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.SingleSoftwareUnit;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.UnboundedSoftwareUnit;
import at.ac.tuwien.dsg.comot.common.model.Strategy;
import static at.ac.tuwien.dsg.comot.common.model.Strategy.Strategy;
import at.ac.tuwien.dsg.orchestrator.interraction.COMOTOrchestrator;
import at.ac.tuwien.dsg.orchestrator.interraction.rsybl.rSYBLInterraction;

/**
 *
 * @author http://dsg.tuwien.ac.at
 */
public class ProgrammingAndControllingElasticityWithCOMOT {

    public static void main(String[] args) {
        //specify service units in terms of software

        String salsaRepo = "http://128.130.172.215/salsa/upload/files/ElasticIoT/";

        //need to specify details of VM and operating system to deploy the software servide units on
        OperatingSystemUnit dataControllerVM = OperatingSystemUnit("DataControllerUnitVM")
                .providedBy(OpenstackSmall()
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );

        OperatingSystemUnit dataNodeVM = OperatingSystemUnit("DataNodeUnitVM")
                .providedBy(OpenstackMicro()
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );

        //finally, we define Vm types for event processing
        OperatingSystemUnit loadbalancerVM = OperatingSystemUnit("LoadBalancerUnitVM")
                .providedBy(OpenstackSmall()
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

        OperatingSystemUnit localProcessingVM = OperatingSystemUnit("LocalProcessingUnitVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("be6ae07b-7deb-4926-bfd7-b11afe228d6a")
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                ).andMinInstances(2);

        OperatingSystemUnit mqttQueueVM = OperatingSystemUnit("MqttQueueVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("17ffd200-315f-4ba8-9e77-c294efc772bd")
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );

        OperatingSystemUnit momVM = OperatingSystemUnit("MoMVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("be6ae07b-7deb-4926-bfd7-b11afe228d6a")
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );

        //start with Data End, and first with Data Controller
        ServiceUnit dataControllerUnit = SingleSoftwareUnit("DataControllerUnit")
                //software artifacts needed for unit deployment   = script to deploy Cassandra
                .deployedBy(SingleScriptArtifactTemplate("deployDataControllerArtifact", salsaRepo + "deployCassandraSeed.sh"))
                //data controller exposed its IP 
                .exposes(Capability.Variable("DataController_IP_information"));

        //specify data node
        ServiceUnit dataNodeUnit = SingleSoftwareUnit("DataNodeUnit")
                .deployedBy(SingleScriptArtifactTemplate("deployDataNodeArtifact", salsaRepo + "deployCassandraNode.sh"))
                //data node MUST KNOW the IP of cassandra seed, to connect to it and join data cluster
                .requires(Requirement.Variable("DataController_IP_Data_Node_Req").withName("requiringDataNodeIP"))
                //express elasticity strategy: Scale IN Data Node when cpu usage < 40%
                .controlledBy(Strategy("DN_ST1")
                        .when(Constraint.MetricConstraint("DN_ST1_CO1", new Metric("cpuUsage", "%")).lessThan("40"))
                        .then(Strategy.Action.ScaleIn)
                );

        //add the service units belonging to the event processing topology
        ServiceUnit momUnit = SingleSoftwareUnit("MOMUnit")
                //load balancer must provide IP
                .exposes(Capability.Variable("MOM_IP_information"))
                .deployedBy(SingleScriptArtifactTemplate("deployMOMArtifact", salsaRepo + "deployQueue.sh"));

        //add the service units belonging to the event processing topology
        ServiceUnit eventProcessingUnit = SingleSoftwareUnit("EventProcessingUnit")
                .deployedBy(SingleScriptArtifactTemplate("deployEventProcessingArtifact", salsaRepo + "deployEventProcessing.sh"))
                //event processing must register in Load Balancer, so it needs the IP
                .requires(Requirement.Variable("EventProcessingUnit_LoadBalancer_IP_Req"))
                //event processing also needs to querry the Data Controller to access data
                .requires(Requirement.Variable("EventProcessingUnit_DataController_IP_Req"))
                .requires(Requirement.Variable("EventProcessingUnit_MOM_IP_Req"))
                //scale IN if throughput < 200 and responseTime < 200
                .controlledBy(Strategy("EP_ST1")
                        .when(Constraint.MetricConstraint("EP_ST1_CO1", new Metric("responseTime", "ms")).lessThan("10"))
                        .and(Constraint.MetricConstraint("EP_ST1_CO2", new Metric("avgThroughput", "operations/s")).lessThan("200"))
                        .then(Strategy.Action.ScaleIn)
                );

        //add the service units belonging to the event processing topology
        ServiceUnit loadbalancerUnit = SingleSoftwareUnit("LoadBalancerUnit")
                //load balancer must provide IP
                .exposes(Capability.Variable("LoadBalancer_IP_information"))
                .deployedBy(SingleScriptArtifactTemplate("deployLoadBalancerArtifact", salsaRepo + "deployLoadBalancer.sh"));

        ServiceUnit mqttUnit = SingleSoftwareUnit("QueueUnit")
                //load balancer must provide IP
                .exposes(Capability.Variable("brokerIp_Capability"))
                .deployedBy(SingleScriptArtifactTemplate("deployMQTTBroker", salsaRepo + "run_mqtt_broker.sh"));

        ServiceUnit localProcessingUnit = SingleSoftwareUnit("LocalProcessingUnit")
                //load balancer must provide IP
                .requires(Requirement.Variable("brokerIp_Requirement"))
                .requires(Requirement.Variable("loadBalancerIp_Requirement"))
                .deployedBy(SingleScriptArtifactTemplate("deployLocalProcessing", salsaRepo + "install-local-analysis-service.sh"));

        //Describe a Data End service topology containing the previous 2 software service units
        ServiceTopology dataEndTopology = ServiceTopology("DataEndTopology")
                .withServiceUnits(dataControllerUnit, dataNodeUnit //add also OS units to topology
                        , dataControllerVM, dataNodeVM
                );

        //specify constraints on the data topology
        //thus, the CPU usage of all Service Unit instances of the data end Topology must be below 80%
        dataEndTopology.constrainedBy(Constraint.MetricConstraint("DET_CO1", new Metric("cpuUsage", "%")).lessThan("80"));

        //define event processing unit topology
        ServiceTopology eventProcessingTopology = ServiceTopology("EventProcessingTopology")
                .withServiceUnits(loadbalancerUnit, eventProcessingUnit, momUnit //add vm types to topology
                        , loadbalancerVM, eventProcessingVM, momVM
                );

        ServiceTopology localProcessinTopology = ServiceTopology("Gateway")
                .withServiceUnits(mqttQueueVM, mqttUnit, localProcessingUnit, localProcessingVM
                );
        localProcessingUnit.
                controlledBy(Strategy("LPT_ST1").when(Constraint.MetricConstraint("LPT_ST1_CO1", new Metric("avgBufferSize", "#")).lessThan("50"))
                        .then(Strategy.Action.ScaleIn));

        localProcessingUnit.constrainedBy(Constraint.MetricConstraint("LPT_CO1", new Metric("avgBufferSize", "#")).lessThan("200"));

        //TODO: de verificat de ce nu converteste ok daca pun si constraints si strategies pe topology
        eventProcessingTopology.constrainedBy(
                Constraint.MetricConstraint("EPT_CO1", new Metric("responseTime", "ms")).lessThan("20"));

        // elasticity capabilities
        dataNodeUnit.provides(
                ElasticityCapability.ScaleOut("ScaleOutDataNode").withPrimitiveOperations("Salsa.scaleOut")
                .withCapabilityEffect(CapabilityEffect(dataNodeUnit)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cpuUsage", "%")).withType(MetricEffect.Type.SUB).withValue(30.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cost", "$")).withType(MetricEffect.Type.ADD).withValue(0.12)))
                .withCapabilityEffect(CapabilityEffect(dataControllerUnit)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cpuUsage", "%")).withType(MetricEffect.Type.SUB).withValue(30.0)))
                .withCapabilityEffect(CapabilityEffect(dataEndTopology)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cpuUsage", "%")).withType(MetricEffect.Type.SUB).withValue(30.0)))
        );
        dataNodeUnit.provides(
                ElasticityCapability.ScaleIn("ScaleInDataNode").withPrimitiveOperations("M2MDaaS.decommissionNode", "Salsa.scaleIn")
                .withCapabilityEffect(CapabilityEffect(dataNodeUnit)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cpuUsage", "%")).withType(MetricEffect.Type.ADD).withValue(30.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cost", "$")).withType(MetricEffect.Type.SUB).withValue(0.12)))
                .withCapabilityEffect(CapabilityEffect(dataControllerUnit)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cpuUsage", "%")).withType(MetricEffect.Type.ADD).withValue(30.0)))
                .withCapabilityEffect(CapabilityEffect(dataEndTopology)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cpuUsage", "%")).withType(MetricEffect.Type.ADD).withValue(30.0)))
        );

        localProcessingUnit.provides(
                ElasticityCapability.ScaleOut("ScaleOutLocalProcessing").withPrimitiveOperations("Salsa.scaleOut")
                .withCapabilityEffect(CapabilityEffect(localProcessingUnit)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("avgBufferSize", "#")).withType(MetricEffect.Type.SUB).withValue(200.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("bufferSize", "#")).withType(MetricEffect.Type.ADD).withValue(500.0)))
                .withCapabilityEffect(CapabilityEffect(localProcessinTopology)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("avgBufferSize", "#")).withType(MetricEffect.Type.SUB).withValue(200.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("bufferSize", "#")).withType(MetricEffect.Type.ADD).withValue(500.0)))
        );

        localProcessingUnit.provides(
                ElasticityCapability.ScaleIn("ScaleInLocalProcessing").withPrimitiveOperations("Salsa.scaleIn")
                .withCapabilityEffect(CapabilityEffect(localProcessingUnit)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("avgBufferSize", "#")).withType(MetricEffect.Type.ADD).withValue(90.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("bufferSize", "#")).withType(MetricEffect.Type.SUB).withValue(200.0)))
                .withCapabilityEffect(CapabilityEffect(localProcessinTopology)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("avgBufferSize", "#")).withType(MetricEffect.Type.ADD).withValue(90.0))
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("bufferSize", "#")).withType(MetricEffect.Type.SUB).withValue(200.0)))
        );

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
        CloudService serviceTemplate = ServiceTemplate("ElasticIoTPlatform")
                .consistsOfTopologies(dataEndTopology)
                .consistsOfTopologies(eventProcessingTopology)
                .consistsOfTopologies(localProcessinTopology)
                //defining CONNECT_TO and HOSTED_ON relationships
                .andRelationships(
                        //Data Controller IP send to Data Node
                        ConnectToRelation("dataNodeToDataController")
                        .from(dataControllerUnit.getContext().get("DataController_IP_information"))
                        .to(dataNodeUnit.getContext().get("DataController_IP_Data_Node_Req")) //specify which software unit goes to which VM
                        ,
                        //event processing gets IP from load balancer
                        ConnectToRelation("eventProcessingToLoadBalancer")
                        .from(loadbalancerUnit.getContext().get("LoadBalancer_IP_information"))
                        .to(eventProcessingUnit.getContext().get("EventProcessingUnit_LoadBalancer_IP_Req")) //specify which software unit goes to which VM
                        ,
                        //event processing gets IP from data controller
                        ConnectToRelation("eventProcessingToDataController")
                        .from(dataControllerUnit.getContext().get("DataController_IP_information"))
                        .to(eventProcessingUnit.getContext().get("EventProcessingUnit_DataController_IP_Req")) //specify which software unit goes to which VM
                        ,
                        ConnectToRelation("eventProcessingToMOM")
                        .from(momUnit.getContext().get("MOM_IP_information"))
                        .to(eventProcessingUnit.getContext().get("EventProcessingUnit_MOM_IP_Req")) //specify which software unit goes to which VM
                        ,
                        ConnectToRelation("mqtt_broker")
                        .from(mqttUnit.getContext().get("brokerIp_Capability"))
                        .to(localProcessingUnit.getContext().get("brokerIp_Requirement")) //specify which software unit goes to which VM
                        ,
                        ConnectToRelation("load_balancer")
                        .from(loadbalancerUnit.getContext().get("LoadBalancer_IP_information"))
                        .to(localProcessingUnit.getContext().get("loadBalancerIp_Requirement")) //specify which software unit goes to which VM
                        ,
                        HostedOnRelation("dataControllerToVM")
                        .from(dataControllerUnit)
                        .to(dataControllerVM),
                        HostedOnRelation("dataNodeToVM")
                        .from(dataNodeUnit)
                        .to(dataNodeVM) //add hosted on relatinos
                        , HostedOnRelation("loadbalancerToVM")
                        .from(loadbalancerUnit)
                        .to(loadbalancerVM),
                        HostedOnRelation("eventProcessingToVM")
                        .from(eventProcessingUnit)
                        .to(eventProcessingVM),
                        HostedOnRelation("momToVM")
                        .from(momUnit)
                        .to(momVM),
                        HostedOnRelation("localProcessingToVM")
                        .from(localProcessingUnit)
                        .to(localProcessingVM),
                        HostedOnRelation("mqttToVM")
                        .from(mqttUnit)
                        .to(mqttQueueVM)
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
        orchestrator.deployAndControl(serviceTemplate);
//        orchestrator.deploy(serviceTemplate);
//        orchestrator.controlExisting(serviceTemplate);
        

    }
}

//
//
//package at.ac.tuwien.dsg.comot.orchestrator;
//
//import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifactTemplate;
//import at.ac.tuwien.dsg.comot.common.model.Capability;
//import at.ac.tuwien.dsg.comot.common.model.CloudService;
//import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackMicro()
//import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall()
//import at.ac.tuwien.dsg.comot.common.model.Constraint;
//import at.ac.tuwien.dsg.comot.common.model.Constraint.Metric;
//import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.ConnectToRelation;
//import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
//import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
//import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
//import at.ac.tuwien.dsg.comot.common.model.Requirement;
//import at.ac.tuwien.dsg.comot.common.model.CloudService;
//import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
//import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
//import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;
//import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
//import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.SingleSoftwareUnit;
//import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.UnboundedSoftwareUnit;
//import at.ac.tuwien.dsg.comot.common.model.Strategy;
//import static at.ac.tuwien.dsg.comot.common.model.Strategy.Strategy;
//import at.ac.tuwien.dsg.orchestrator.interraction.COMOTOrchestrator;
//
///**
// *
// * @author http://dsg.tuwien.ac.at
// */
//public class ProgrammingAndControllingElasticityWithCOMOT {
//    
//    public static void main(String[] args) {
//        //specify service units in terms of software
//
//        //need to specify details of VM and operating system to deploy the software servide units on
//        OperatingSystemUnit dataControllerVM = OperatingSystemUnit("DataControllerUnitVM")
//                .providedBy(OpenstackSmall()
//                        .addSoftwarePackage("openjdk-7-jre")
//                        .addSoftwarePackage("ganglia-monitor")
//                        .addSoftwarePackage("gmetad")
//                );
//        
//        OperatingSystemUnit dataNodeVM = OperatingSystemUnit("DataNodeUnitVM")
//                .providedBy(OpenstackMicro()
//                        .addSoftwarePackage("openjdk-7-jre")
//                        .addSoftwarePackage("ganglia-monitor")
//                        .addSoftwarePackage("gmetad")
//                );
//
//        //finally, we define Vm types for event processing
//        OperatingSystemUnit loadbalancerVM = OperatingSystemUnit("LoadBalancerUnitVM")
//                .providedBy(OpenstackSmall()
//                        .addSoftwarePackage("openjdk-7-jre")
//                        .addSoftwarePackage("ganglia-monitor")
//                        .addSoftwarePackage("gmetad")
//                );
//        
//        OperatingSystemUnit eventProcessingVM = OperatingSystemUnit("EventProcessingUnitVM")
//                .providedBy(OpenstackSmall()
//                        .withBaseImage("be6ae07b-7deb-4926-bfd7-b11afe228d6a")
//                        .addSoftwarePackage("openjdk-7-jre")
//                        .addSoftwarePackage("ganglia-monitor")
//                        .addSoftwarePackage("gmetad")
//                );
//        
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
//        
//        OperatingSystemUnit momVM = OperatingSystemUnit("MoMVM")
//                .providedBy(OpenstackSmall()
//                        .withBaseImage("be6ae07b-7deb-4926-bfd7-b11afe228d6a")
//                        .addSoftwarePackage("openjdk-7-jre")
//                        .addSoftwarePackage("ganglia-monitor")
//                        .addSoftwarePackage("gmetad")
//                );
//
//        //start with Data End, and first with Data Controller
//        ServiceUnit dataControllerUnit = SingleSoftwareUnit("DataControllerUnit")
//                //software artifacts needed for unit deployment   = script to deploy Cassandra
//                .deployedBy(SingleScriptArtifactTemplate("deployDataControllerArtifact", "http://128.130.172.215/salsa/upload/files/DaasService/deployCassandraSeed.sh"))
//                //data controller exposed its IP 
//                .exposes(Capability.Variable("DataController_IP_information"));
//
//        //specify data node
//        ServiceUnit dataNodeUnit = SingleSoftwareUnit("DataNodeUnit")
//                .deployedBy(SingleScriptArtifactTemplate("deployDataNodeArtifact", "http://128.130.172.215/salsa/upload/files/DaasService/deployCassandraNode.sh"))
//                //data node MUST KNOW the IP of cassandra seed, to connect to it and join data cluster
//                .requires(Requirement.Variable("DataController_IP_Data_Node_Req").withName("requiringDataNodeIP"))
//                //express elasticity strategy: Scale IN Data Node when cpu usage < 40%
//                .controlledBy(Strategy("ST1")
//                        .when(Constraint.MetricConstraint("ST1CO1", new Metric("cpuUsage", "%")).lessThan("40"))
//                        .then(Strategy.Action.ScaleIn)
//                );
//
//        //add the service units belonging to the event processing topology
//        ServiceUnit momUnit = SingleSoftwareUnit("MOMUnit")
//                //load balancer must provide IP
//                .exposes(Capability.Variable("MOM_IP_information"))
//                .deployedBy(SingleScriptArtifactTemplate("deployMOMArtifact", "http://128.130.172.215/salsa/upload/files/DaasService/deployQueue.sh"));
//
//        //add the service units belonging to the event processing topology
//        ServiceUnit eventProcessingUnit = SingleSoftwareUnit("EventProcessingUnit")
//                .deployedBy(SingleScriptArtifactTemplate("deployEventProcessingArtifact", "http://128.130.172.215/salsa/upload/files/DaasService/deployEventProcessing.sh"))
//                //event processing must register in Load Balancer, so it needs the IP
//                .requires(Requirement.Variable("EventProcessingUnit_LoadBalancer_IP_Req"))
//                //event processing also needs to querry the Data Controller to access data
//                .requires(Requirement.Variable("EventProcessingUnit_DataController_IP_Req"))
//                .requires(Requirement.Variable("EventProcessingUnit_MOM_IP_Req"))
//                //scale IN if throughput < 200 and responseTime < 200
//                .controlledBy(Strategy("ST2")
//                        .when(Constraint.MetricConstraint("ST2CO1", new Metric("responseTime", "ms")).lessThan("10"))
//                        .and(Constraint.MetricConstraint("ST2CO2", new Metric("avgThroughput", "operations/s")).lessThan("200"))
//                        .then(Strategy.Action.ScaleIn)
//                );
//
//        //add the service units belonging to the event processing topology
//        ServiceUnit loadbalancerUnit = SingleSoftwareUnit("LoadBalancerUnit")
//                //load balancer must provide IP
//                .exposes(Capability.Variable("LoadBalancer_IP_information"))
//                .deployedBy(SingleScriptArtifactTemplate("deployLoadBalancerArtifact", "http://128.130.172.215/salsa/upload/files/DaasService/deployLoadBalancer.sh"));
//        
//        ServiceUnit mqttUnit = SingleSoftwareUnit("QueueUnit")
//                //load balancer must provide IP
//                .exposes(Capability.Variable("brokerIp_Capability"))
//                .deployedBy(SingleScriptArtifactTemplate("deployMQTTBroker", "http://128.130.172.215/salsa/upload/files/DaasService/IoT/run_mqtt_broker.sh"));
//        
//        ServiceUnit localProcessingUnit = SingleSoftwareUnit("LocalProcessingUnit")
//                //load balancer must provide IP
//                .requires(Requirement.Variable("brokerIp_Requirement"))
//                .requires(Requirement.Variable("loadBalancerIp_Requirement"))
//                .deployedBy(SingleScriptArtifactTemplate("deployLocalProcessing", "http://128.130.172.215/salsa/upload/files/DaasService/IoT/install-local-analysis-service.sh"));
//
//        //Describe a Data End service topology containing the previous 2 software service units
//        ServiceTopology dataEndTopology = ServiceTopology("DataEndTopology")
//                .withServiceUnits(dataControllerUnit, dataNodeUnit //add also OS units to topology
//                        , dataControllerVM, dataNodeVM
//                );
//
//        //specify constraints on the data topology
//        //thus, the CPU usage of all Service Unit instances of the data end Topology must be below 80%
//        dataEndTopology.constrainedBy(Constraint.MetricConstraint("DataEndCO1", new Metric("cpuUsage", "%")).lessThan("80"));
//
//        //define event processing unit topology
//        ServiceTopology eventProcessingTopology = ServiceTopology("EventProcessingTopology")
//                .withServiceUnits(loadbalancerUnit, eventProcessingUnit, momUnit //add vm types to topology
//                        , loadbalancerVM, eventProcessingVM, momVM
//                );
//        
//        ServiceTopology localProcessinTopology = ServiceTopology("Gateway")
//                .withServiceUnits(mqttQueueVM, mqttUnit, localProcessingUnit, localProcessingVM
//                );
//        localProcessinTopology.
//                controlledBy(Strategy("St1").when(Constraint.MetricConstraint("ST1CO1", new Metric("avgBufferSize", "#")).lessThan("50"))
//                .then(Strategy.Action.ScaleIn))
//                .constrainedBy(Constraint.MetricConstraint("CO1", new Metric("avgBufferSize", "#")).lessThan("100"));
//        
//        eventProcessingTopology.constrainedBy(Constraint.MetricConstraint("C02", new Metric("responseTime", "ms")).lessThan("30"));
//
//        //describe the service template which will hold more topologies
//        CloudService serviceTemplate = ServiceTemplate("IoTDaaSControl")
//                .consistsOfTopologies(dataEndTopology)
//                .consistsOfTopologies(eventProcessingTopology)
//                .consistsOfTopologies(localProcessinTopology)
//                //defining CONNECT_TO and HOSTED_ON relationships
//                .andRelationships(
//                        //Data Controller IP send to Data Node
//                        ConnectToRelation("dataNodeToDataController")
//                        .from(dataControllerUnit.getContext().get("DataController_IP_information"))
//                        .to(dataNodeUnit.getContext().get("DataController_IP_Data_Node_Req")) //specify which software unit goes to which VM
//                        ,
//                        //event processing gets IP from load balancer
//                        ConnectToRelation("eventProcessingToLoadBalancer")
//                        .from(loadbalancerUnit.getContext().get("LoadBalancer_IP_information"))
//                        .to(eventProcessingUnit.getContext().get("EventProcessingUnit_LoadBalancer_IP_Req")) //specify which software unit goes to which VM
//                        ,
//                        //event processing gets IP from data controller
//                        ConnectToRelation("eventProcessingToDataController")
//                        .from(dataControllerUnit.getContext().get("DataController_IP_information"))
//                        .to(eventProcessingUnit.getContext().get("EventProcessingUnit_DataController_IP_Req")) //specify which software unit goes to which VM
//                        ,
//                        ConnectToRelation("eventProcessingToMOM")
//                        .from(momUnit.getContext().get("MOM_IP_information"))
//                        .to(eventProcessingUnit.getContext().get("EventProcessingUnit_MOM_IP_Req")) //specify which software unit goes to which VM
//                        ,
//                        ConnectToRelation("mqtt_broker")
//                        .from(mqttUnit.getContext().get("brokerIp_Capability"))
//                        .to(localProcessingUnit.getContext().get("brokerIp_Requirement")) //specify which software unit goes to which VM
//                        ,
//                        ConnectToRelation("load_balancer")
//                        .from(loadbalancerUnit.getContext().get("LoadBalancer_IP_information"))
//                        .to(localProcessingUnit.getContext().get("loadBalancerIp_Requirement")) //specify which software unit goes to which VM
//                        ,
//                        HostedOnRelation("dataControllerToVM")
//                        .from(dataControllerUnit)
//                        .to(dataControllerVM),
//                        HostedOnRelation("dataNodeToVM")
//                        .from(dataNodeUnit)
//                        .to(dataNodeVM) //add hosted on relatinos
//                        , HostedOnRelation("loadbalancerToVM")
//                        .from(loadbalancerUnit)
//                        .to(loadbalancerVM),
//                        HostedOnRelation("eventProcessingToVM")
//                        .from(eventProcessingUnit)
//                        .to(eventProcessingVM),
//                        HostedOnRelation("momToVM")
//                        .from(momUnit)
//                        .to(momVM),
//                        HostedOnRelation("localProcessingToVM")
//                        .from(localProcessingUnit)
//                        .to(localProcessingVM),
//                        HostedOnRelation("mqttToVM")
//                        .from(mqttUnit)
//                        .to(mqttQueueVM)
//                )
//                // as we have horizontally scalable distributed systems (one service unit can have more instances)
//                //metrics must be aggregated among VMs
//                .withDefaultMetrics()
//                //to find scaling actions, one must assume some effects for each action, to understand
//                //if it makes sense or not to execute the action
//                .withDefaultActionEffects();
//
//        //instantiate COMOT orchestrator to deploy, monitor and control the service
//        COMOTOrchestrator orchestrator = new COMOTOrchestrator()
//                //we have SALSA as cloud management tool
//                //curently deployed separately
//                .withSalsaIP("128.130.172.215")
//                .withSalsaPort(8080)
//                //we have rSYBL elasticity control service and MELA 
//                //deployed separately
//                .withRsyblIP("128.130.172.214")
//                //                .withRsyblIP("localhost")
//                //                .withRsyblIP("109.231.121.66")
//                .withRsyblPort(8280);
//
//        //deploy, monitor and control
////        orchestrator.deployAndControl(serviceTemplate);
////        orchestrator.deploy(serviceTemplate);
//        orchestrator.controlExisting(serviceTemplate);
//        
//    }
//}
