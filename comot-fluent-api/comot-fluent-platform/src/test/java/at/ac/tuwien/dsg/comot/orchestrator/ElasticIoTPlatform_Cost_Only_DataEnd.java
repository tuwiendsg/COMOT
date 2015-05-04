package at.ac.tuwien.dsg.comot.orchestrator;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifact;
import static at.ac.tuwien.dsg.comot.common.model.BASHAction.BASHAction;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import static at.ac.tuwien.dsg.comot.common.model.CapabilityEffect.CapabilityEffect;
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
public class ElasticIoTPlatform_Cost_Only_DataEnd {

    public static void main(String[] args) {
        //specify service units in terms of software

         String salsaRepo = "http://128.130.172.215/salsa/upload/files/TMP/Daniel_DataEnd_Cost/";

        //need to specify details of VM and operating system to deploy the software servide units on
        OperatingSystemUnit dataControllerVM = OperatingSystemUnit("DataControllerUnitVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("be6ae07b-7deb-4926-bfd7-b11afe228d6a")
                );

        OperatingSystemUnit dataNodeVM = OperatingSystemUnit("DataNodeUnitVM")
                .providedBy(OpenstackMicro()
                        .withBaseImage("be6ae07b-7deb-4926-bfd7-b11afe228d6a")
                );

        final ServiceUnit loadbalancerVM = OperatingSystemUnit("LoadBalancerUnitVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("be6ae07b-7deb-4926-bfd7-b11afe228d6a")
                //                        .addSoftwarePackage("ganglia-monitor")
                //                        .addSoftwarePackage("gmetad")
                )
                .andReference("EventProcessingTopology_COMMON/LoadBalancerUnitVM");

        OperatingSystemUnit eventProcessingVM = OperatingSystemUnit("EventProcessingUnitVM")
                .providedBy(OpenstackSmall()
                        .withBaseImage("be6ae07b-7deb-4926-bfd7-b11afe228d6a")
                );

        //start with Data End, and first with Data Controller
        ServiceUnit dataControllerUnit = SingleSoftwareUnit("DataControllerUnit")
                //software artifacts needed for unit deployment   = script to deploy Cassandra
                .deployedBy(SingleScriptArtifact("deployDataControllerArtifact", salsaRepo + "deployCassandraSeed.sh"))
                //data controller exposed its IP 
                .withMaxColocatedInstances(1)
                .exposes(Capability.Variable("DataController_IP_information"));

        //specify data node
        ServiceUnit dataNodeUnit = SingleSoftwareUnit("DataNodeUnit")
                .deployedBy(SingleScriptArtifact("deployDataNodeArtifact", salsaRepo + "deployCassandraNode.sh"))
                //data node MUST KNOW the IP of cassandra seed, to connect to it and join data cluster
                .requires(Requirement.Variable("DataController_IP_Data_Node_Req").withName("requiringDataNodeIP"))
                //.provides(dataNodeUnitScaleIn, dataNodeUnitScaleOut)
                //express elasticity strategy: Scale IN Data Node when cpu usage < 40%
                .withMaxColocatedInstances(1)
                .withLifecycleAction(LifecyclePhase.STOP, BASHAction("sudo service joinRing stop"));

        //add the service units belonging to the event processing topology
        ServiceUnit eventProcessingUnit = SingleSoftwareUnit("EventProcessingUnit")
                .deployedBy(SingleScriptArtifact("deployEventProcessingArtifact", salsaRepo + "deployEventProcessing.sh"))
                .requires(Requirement.Variable("EventProcessingUnit_LoadBalancer_IP_Req"))
                .requires(Requirement.Variable("EventProcessingUnit_DataController_IP_Req"))
                .withMaxColocatedInstances(1)
                .withLifecycleAction(LifecyclePhase.STOP, BASHAction("sudo service event-processing stop"));

        //add the service units belonging to the event processing topology
        final ServiceUnit loadbalancerUnit = SingleSoftwareUnit("LoadBalancerUnit")
                //load balancer must provide IP
                .exposes(Capability.Variable("LoadBalancer_IP_information"))
                .deployedBy(SingleScriptArtifact("deployLoadBalancerArtifact", salsaRepo + "deployLoadBalancer.sh"))
                .withMaxColocatedInstances(1)
                .andReference("EventProcessingTopology_COMMON/LoadBalancerUnit");

        //Describe a Data End service topology containing the previous 2 software service units
        ServiceTopology dataEndTopology = ServiceTopology("DataEndTopology")
                .withServiceUnits(dataControllerUnit, dataNodeUnit //add also OS units to topology
                        , dataControllerVM, dataNodeVM
                );
        
          ServiceTopology eventProcessingTopology = ServiceTopology("EventProcessingTopology")
                .withServiceUnits(eventProcessingUnit, eventProcessingVM, loadbalancerUnit, loadbalancerVM
                );

        //specify constraints on the data topology
        //thus, the CPU usage of all Service Unit instances of the data end Topology must be below 80%
        dataEndTopology.constrainedBy(Constraint.MetricConstraint("DET_CO1", new Metric("cpuUsage", "%")).lessThan("80"));

        final COMOTOrchestrator orchestrator = new COMOTOrchestrator().withSalsaIP("128.130.172.215").withSalsaPort(8380).withRsyblIP("128.130.172.215").withRsyblPort(8280);

//        String[] strategies = new String[]{"STRATEGY_LAST_ADDED", "STRATEGY_FIRST_ADDED", "STRATEGY_MELA_COST_RECOMMENDATION_LIFETIME", "STRATEGY_MELA_COST_RECOMMENDATION_EFFICIENCY"};
        String[] strategies = new String[]{"STRATEGY_MELA_COST_RECOMMENDATION_EFFICIENCY"};

//        List<Thread> threads = new ArrayList<>();
        for (final String strategy : strategies) {
//            Thread t = new Thread() {
//
//                @Override
//                public void run() {
            //describe the service template which will hold more topologies
            CloudService serviceTemplate = ServiceTemplate("DataEndTopology_" + strategy)
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
                    .withDefaultMetrics();

            orchestrator.deploy(serviceTemplate);
//                }

//            };
//            t.setDaemon(true);
//            threads.add(t);
//            t.start();
        }
    }
}
