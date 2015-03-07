package at.ac.tuwien.dsg.comot.orchestrator;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifact;
import static at.ac.tuwien.dsg.comot.common.model.BASHAction.BASHAction;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import static at.ac.tuwien.dsg.comot.common.model.CapabilityEffect.CapabilityEffect;
import static at.ac.tuwien.dsg.comot.common.model.MetricEffect.MetricEffect;
import at.ac.tuwien.dsg.comot.common.model.Constraint;
import at.ac.tuwien.dsg.comot.common.model.Constraint.Metric;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.ConnectToRelation;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.Requirement;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification;
import at.ac.tuwien.dsg.comot.common.model.ElasticityCapability;
import at.ac.tuwien.dsg.comot.common.model.LifecyclePhase;
import at.ac.tuwien.dsg.comot.common.model.MetricEffect;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
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
public class ProgrammingAndControllingElasticityWithCOMOTFlexiant {

    public static void main(String[] args) {
        //specify service units in terms of software

        String salsaRepo = "http://128.130.172.215/salsa/upload/files/DaaS_Flexiant/";

        //need to specify details of VM and operating system to deploy the software servide units on
        OperatingSystemUnit dataControllerVM = OperatingSystemUnit("DataControllerUnitVM")
                .providedBy(CommonOperatingSystemSpecification.FlexiantSmall()
                );

        OperatingSystemUnit dataNodeVM = OperatingSystemUnit("DataNodeUnitVM")
                .providedBy(CommonOperatingSystemSpecification.FlexiantSmall()
                );

        //finally, we define Vm types for event processing
        OperatingSystemUnit loadbalancerVM = OperatingSystemUnit("LoadBalancerUnitVM")
                .providedBy(CommonOperatingSystemSpecification.FlexiantSmall()
                );

        OperatingSystemUnit eventProcessingVM = OperatingSystemUnit("EventProcessingUnitVM")
                .providedBy(CommonOperatingSystemSpecification.FlexiantSmall()
                );

        OperatingSystemUnit momVM = OperatingSystemUnit("MoMVM")
                .providedBy(CommonOperatingSystemSpecification.FlexiantSmall()
                );

        //start with Data End, and first with Data Controller
        ServiceUnit dataControllerUnit = SingleSoftwareUnit("DataControllerUnit")
                //software artifacts needed for unit deployment   = script to deploy Cassandra
                .deployedBy(SingleScriptArtifact("deployDataControllerArtifact", salsaRepo + "deployCassandraSeed.sh"))
                //data controller exposed its IP 
                .exposes(Capability.Variable("DataController_IP_information"));
//
        ElasticityCapability dataNodeUnitScaleIn = ElasticityCapability.ScaleIn().withPrimitiveOperations("M2MDaaS.decommissionNode", "Salsa.scaleIn");
        ElasticityCapability dataNodeUnitScaleOut = ElasticityCapability.ScaleOut().withPrimitiveOperations("Salsa.scaleOut");

        //specify data node
        ServiceUnit dataNodeUnit = SingleSoftwareUnit("DataNodeUnit")
                .deployedBy(SingleScriptArtifact("deployDataNodeArtifact", salsaRepo + "deployCassandraNode.sh"))
                //data node MUST KNOW the IP of cassandra seed, to connect to it and join data cluster
                .requires(Requirement.Variable("DataController_IP_Data_Node_Req").withName("requiringDataNodeIP"))
                .provides(dataNodeUnitScaleIn, dataNodeUnitScaleOut)
                //express elasticity strategy: Scale IN Data Node when cpu usage < 40%
                .controlledBy(Strategy("DN_ST1")
                        .when(Constraint.MetricConstraint("DN_ST1_CO1", new Metric("cpuUsage", "%")).lessThan("40"))
                        .enforce(dataNodeUnitScaleIn)
                )
                //              
                .withLifecycleAction(LifecyclePhase.STOP, BASHAction("sudo /bin/decommission"));
        ;

        //add the service units belonging to the event processing topology
        ServiceUnit momUnit = SingleSoftwareUnit("MOMUnit")
                //load balancer must provide IP
                .exposes(Capability.Variable("MOM_IP_information"))
                .deployedBy(SingleScriptArtifact("deployMOMArtifact", salsaRepo + "deployQueue.sh"));

        ElasticityCapability eventProcessingUnitScaleIn = ElasticityCapability.ScaleIn();
        ElasticityCapability eventProcessingUnitScaleOut = ElasticityCapability.ScaleOut();

        //add the service units belonging to the event processing topology
        ServiceUnit eventProcessingUnit = SingleSoftwareUnit("EventProcessingUnit")
                .deployedBy(SingleScriptArtifact("deployEventProcessingArtifact", salsaRepo + "deployEventProcessing.sh"))
                //event processing must register in Load Balancer, so it needs the IP
                .requires(Requirement.Variable("EventProcessingUnit_LoadBalancer_IP_Req"))
                //event processing also needs to querry the Data Controller to access data
                .requires(Requirement.Variable("EventProcessingUnit_DataController_IP_Req"))
                .requires(Requirement.Variable("EventProcessingUnit_MOM_IP_Req"))
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

        ServiceUnit mqttUnit = SingleSoftwareUnit("QueueUnit")
                //load balancer must provide IP
                .exposes(Capability.Variable("brokerIp_Capability"))
                .deployedBy(SingleScriptArtifact("deployMQTTBroker", salsaRepo + "run_mqtt_broker.sh"));
//
        ElasticityCapability localProcessingUnitScaleIn = ElasticityCapability.ScaleIn().withPrimitiveOperations("Salsa.scaleIn");
        ElasticityCapability localProcessingUnitScaleOut = ElasticityCapability.ScaleOut().withPrimitiveOperations("Salsa.scaleOut");

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

        eventProcessingTopology.constrainedBy(
                Constraint.MetricConstraint("EPT_CO1", new Metric("responseTime", "ms")).lessThan("20"));

        // elasticity capabilities
        dataNodeUnitScaleOut.withCapabilityEffect(CapabilityEffect(dataNodeUnit)
                .withMetricEffect(
                        MetricEffect().withMetric(new Metric("cpuUsage", "%")).withType(MetricEffect.Type.SUB).withValue(30.0))
                .withMetricEffect(
                        MetricEffect().withMetric(new Metric("cost", "$")).withType(MetricEffect.Type.ADD).withValue(0.12)))
                .withCapabilityEffect(CapabilityEffect(dataControllerUnit)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cpuUsage", "%")).withType(MetricEffect.Type.SUB).withValue(30.0)))
                .withCapabilityEffect(CapabilityEffect(dataEndTopology)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cpuUsage", "%")).withType(MetricEffect.Type.SUB).withValue(30.0)));

        dataNodeUnitScaleIn.withCapabilityEffect(CapabilityEffect(dataNodeUnit)
                .withMetricEffect(
                        MetricEffect().withMetric(new Metric("cpuUsage", "%")).withType(MetricEffect.Type.ADD).withValue(30.0))
                .withMetricEffect(
                        MetricEffect().withMetric(new Metric("cost", "$")).withType(MetricEffect.Type.SUB).withValue(0.12)))
                .withCapabilityEffect(CapabilityEffect(dataControllerUnit)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cpuUsage", "%")).withType(MetricEffect.Type.ADD).withValue(30.0)))
                .withCapabilityEffect(CapabilityEffect(dataEndTopology)
                        .withMetricEffect(
                                MetricEffect().withMetric(new Metric("cpuUsage", "%")).withType(MetricEffect.Type.ADD).withValue(30.0)));

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
        CloudService serviceTemplate = ServiceTemplate("ElasticIoT")
                .consistsOfTopologies(dataEndTopology)
                .consistsOfTopologies(eventProcessingTopology)
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
                        HostedOnRelation("momToVM")
                        .from(momUnit)
                        .to(momVM),
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
                        .to(eventProcessingVM)
                )
                // as we have horizontally scalable distributed systems (one service unit can have more instances)
                //metrics must be aggregated among VMs
                .withDefaultMetrics();

        COMOTOrchestrator orchestrator = new COMOTOrchestrator();

        orchestrator.deployAndControl(serviceTemplate);

    }
}
