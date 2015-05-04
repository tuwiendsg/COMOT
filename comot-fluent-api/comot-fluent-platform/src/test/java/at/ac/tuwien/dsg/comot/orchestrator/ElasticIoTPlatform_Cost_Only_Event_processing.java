package at.ac.tuwien.dsg.comot.orchestrator;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifact;
import static at.ac.tuwien.dsg.comot.common.model.BASHAction.BASHAction;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.FlexiantMicro;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.FlexiantSmall;
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
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.SingleSoftwareUnit;
import static at.ac.tuwien.dsg.comot.common.model.Strategy.Strategy;
import at.ac.tuwien.dsg.comot.orchestrator.interraction.COMOTOrchestrator;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author http://dsg.tuwien.ac.at
 */
/**
 * Only Load Balancer and Event Processing
 *
 * @author daniel-tuwien
 */
public class ElasticIoTPlatform_Cost_Only_Event_processing {

    public static void main(String[] args) {
        //specify service units in terms of software

        String salsaRepo = "http://109.231.121.57/EventProcessingNoDB/";

        //finally, we define Vm types for event processing
        final ServiceUnit loadbalancerVM = OperatingSystemUnit("LoadBalancerUnitVM")
                .providedBy(FlexiantMicro()
                        .withBaseImage("4ddb13c2-ce8a-36f9-a95f-87f34b1fd64a")
                        //                        .addSoftwarePackage("ganglia-monitor")
                        //                        .addSoftwarePackage("gmetad")
                                        )
//                        ;
                        .andReference("EventProcessingTopology_COMMON/LoadBalancerUnitVM");

        final ServiceUnit eventProcessingVM = OperatingSystemUnit("EventProcessingUnitVM")
                .providedBy(FlexiantMicro()
                        .withBaseImage("4ddb13c2-ce8a-36f9-a95f-87f34b1fd64a")
                //                        .addSoftwarePackage("openjdk-7-jre")
                //                        .addSoftwarePackage("ganglia-monitor")
                //                        .addSoftwarePackage("gmetad")
                );

        ElasticityCapability eventProcessingUnitScaleIn = ElasticityCapability.ScaleIn();
        ElasticityCapability eventProcessingUnitScaleOut = ElasticityCapability.ScaleOut();

        //add the service units belonging to the event processing topology
        final ServiceUnit eventProcessingUnit = SingleSoftwareUnit("EventProcessingUnit")
                .deployedBy(SingleScriptArtifact("deployEventProcessingArtifact", salsaRepo + "deployEventProcessing.sh"))
                //event processing must register in Load Balancer, so it needs the IP
                .requires(Requirement.Variable("A"))
                //event processing also needs to querry the Data Controller to access data
                //                .provides(eventProcessingUnitScaleIn, eventProcessingUnitScaleOut)
                //scale IN if throughput < 200 and responseTime < 200
                .controlledBy(Strategy("EP_ST1")
                        .when(Constraint.MetricConstraint("EP_ST1_CO1", new Metric("responseTime", "ms")).lessThan("10"))
                        .enforce(eventProcessingUnitScaleIn)
                ).controlledBy(Strategy("EP_ST2")
                        .when(Constraint.MetricConstraint("EP_ST1_CO2", new Metric("responseTime", "ms")).greaterThan("50"))
                        .enforce(eventProcessingUnitScaleOut)
                ).withMinInstances(0).withMaxColocatedInstances(1)
                .withLifecycleAction(LifecyclePhase.STOP, BASHAction("sudo service event-processing stop"));

        //add the service units belonging to the event processing topology
        final ServiceUnit loadbalancerUnit = SingleSoftwareUnit("LoadBalancerUnit")
                //load balancer must provide IP
                .exposes(Capability.Variable("A"))
                .deployedBy(SingleScriptArtifact("deployLoadBalancerArtifact", salsaRepo + "deployLoadBalancer.sh"))
                .withMaxColocatedInstances(1)
                //                ;
                .andReference("EventProcessingTopology_COMMON/LoadBalancerUnit");

        final ServiceTopology eventProcessingTopology = ServiceTopology("EventProcessingTopology")
                .withServiceUnits(loadbalancerUnit, eventProcessingUnit //add vm types to topology
                        , loadbalancerVM, eventProcessingVM
                );

        final COMOTOrchestrator orchestrator = new COMOTOrchestrator().withSalsaIP("109.231.121.57").withSalsaPort(8380).withRsyblIP("128.130.172.215").withRsyblPort(8280);

//          String[] strategies = new String[]{"STRATEGY_LAST_ADDED", "STRATEGY_FIRST_ADDED", "STRATEGY_MELA_COST_RECOMMENDATION_LIFETIME", "STRATEGY_MELA_COST_RECOMMENDATION_EFFICIENCY"};
        String[] strategies = new String[]{"AA"};

//        List<Thread> threads = new ArrayList<>();
        for (final String strategy : strategies) {
//            Thread t = new Thread() {
//
//                @Override
//                public void run() {
            //describe the service template which will hold more topologies
            CloudService serviceTemplate = ServiceTemplate("EventProcessingTopology_" + strategy)
                    .consistsOfTopologies(eventProcessingTopology)
                    //                .consistsOfTopologies(localProcessinTopology)
                    //defining CONNECT_TO and HOSTED_ON relationships
                    .andRelationships(
                            //event processing gets IP from load balancer
                            ConnectToRelation("eventProcessingToLoadBalancer")
                            .from(loadbalancerUnit.getContext().get("A"))
                            .to(eventProcessingUnit.getContext().get("A")),
                            HostedOnRelation("loadbalancerToVM")
                            .from(loadbalancerUnit)
                            .to(loadbalancerVM),
                            HostedOnRelation("eventProcessingToVM")
                            .from(eventProcessingUnit)
                            .to(eventProcessingVM)
                    )
                    // as we have horizontally scalable distributed systems (one service unit can have more instances)
                    //metrics must be aggregated among VMs
                    .withDefaultMetrics();

            orchestrator.deploy(serviceTemplate);
//                }

//            };
//            t.setDaemon(true);
//            threads.add(t);
//            t.start();
        }

//        for (Thread t : threads) {
//            try {
//                t.join();
//            } catch (InterruptedException ex) {
//                Logger.getLogger(ElasticIoTPlatform_Cost_Only_Event_processing.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
    }
}
