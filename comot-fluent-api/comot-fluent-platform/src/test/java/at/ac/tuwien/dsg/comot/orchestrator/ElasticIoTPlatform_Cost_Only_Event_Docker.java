package at.ac.tuwien.dsg.comot.orchestrator;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifact;
import static at.ac.tuwien.dsg.comot.common.model.BASHAction.BASHAction;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import at.ac.tuwien.dsg.comot.common.model.Constraint;
import at.ac.tuwien.dsg.comot.common.model.Constraint.Metric;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.ConnectToRelation;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.Requirement;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.DockerDefault;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.LocalDocker;
import static at.ac.tuwien.dsg.comot.common.model.DockerUnit.DockerUnit;
import at.ac.tuwien.dsg.comot.common.model.DockerUnit;
import at.ac.tuwien.dsg.comot.common.model.ElasticityCapability;
import at.ac.tuwien.dsg.comot.common.model.LifecyclePhase;
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
public class ElasticIoTPlatform_Cost_Only_Event_Docker {

    public static void main(String[] args) {
        //specify service units in terms of software

        String salsaRepo = "http://128.130.172.215/salsa/upload/files/TMP/Daniel_Cost_Docker/";

        //define localhost docker 
        OperatingSystemUnit privateVM = OperatingSystemUnit("PersonalLaptop")
                .providedBy(LocalDocker()
                );

        //then, we define Docker types for event processing
        DockerUnit loadbalancerDocker = DockerUnit("LoadBalancerUnitDocker")
                .providedBy(DockerDefault()
                );

        DockerUnit eventProcessingDocker = DockerUnit("EventProcessingUnitDocker")
                .providedBy(DockerDefault()
                );

        ElasticityCapability eventProcessingUnitScaleIn = ElasticityCapability.ScaleIn();
        ElasticityCapability eventProcessingUnitScaleOut = ElasticityCapability.ScaleOut();

        //add the service units belonging to the event processing topology
        ServiceUnit eventProcessingUnit = SingleSoftwareUnit("EventProcessingUnit")
                .deployedBy(SingleScriptArtifact("deployEventProcessingArtifact", salsaRepo + "deployEventProcessing.sh"))
                //event processing must register in Load Balancer, so it needs the IP
                .requires(Requirement.Variable("EventProcessingUnit_LoadBalancer_IP_Req"))
                //event processing also needs to querry the Data Controller to access data
                //                .provides(eventProcessingUnitScaleIn, eventProcessingUnitScaleOut)
                //scale IN if throughput < 200 and responseTime < 200
                .controlledBy(Strategy("EP_ST1")
                        .when(Constraint.MetricConstraint("EP_ST1_CO1", new Metric("responseTime", "ms")).lessThan("10"))
                        .enforce(eventProcessingUnitScaleIn)
                ).controlledBy(Strategy("EP_ST2")
                        .when(Constraint.MetricConstraint("EP_ST1_CO2", new Metric("responseTime", "ms")).greaterThan("50"))
                        .enforce(eventProcessingUnitScaleOut)
                ).withMaxColocatedInstances(1)
                .withLifecycleAction(LifecyclePhase.STOP, BASHAction("sudo service event-processing stop"));

        //add the service units belonging to the event processing topology
        ServiceUnit loadbalancerUnit = SingleSoftwareUnit("LoadBalancerUnit")
                //load balancer must provide IP
                .exposes(Capability.Variable("LoadBalancer_IP_information"))
                .deployedBy(SingleScriptArtifact("deployLoadBalancerArtifact", salsaRepo + "deployLoadBalancer.sh"))
                .withMaxColocatedInstances(1);

        ServiceTopology eventProcessingTopology = ServiceTopology("EventProcessingTopology")
                .withServiceUnits(loadbalancerUnit, eventProcessingUnit //add vm types to topology
                        , loadbalancerDocker, eventProcessingDocker, privateVM
                );

        //describe the service template which will hold more topologies
        CloudService serviceTemplate = ServiceTemplate("EventProcessingTopologyCostDaniel")
                .consistsOfTopologies(eventProcessingTopology)
                //                .consistsOfTopologies(localProcessinTopology)
                //defining CONNECT_TO and HOSTED_ON relationships
                .andRelationships(
                        //event processing gets IP from load balancer
                        ConnectToRelation("eventProcessingToLoadBalancer")
                        .from(loadbalancerUnit.getContext().get("LoadBalancer_IP_information"))
                        .to(eventProcessingUnit.getContext().get("EventProcessingUnit_LoadBalancer_IP_Req")),
                        HostedOnRelation("loadbalancerToVM")
                        .from(loadbalancerUnit)
                        .to(loadbalancerDocker),
                        HostedOnRelation("eventProcessingToVM")
                        .from(eventProcessingUnit)
                        .to(eventProcessingDocker),
                        HostedOnRelation("eventProcessingDockerToVM")
                        .from(eventProcessingDocker)
                        .to(privateVM)
                )
                // as we have horizontally scalable distributed systems (one service unit can have more instances)
                //metrics must be aggregated among VMs
                .withDefaultMetrics();
        //to find scaling actions, one must assume some effects for each action, to understand
        //if it makes sense or not to execute the action
//                .withDefaultActionEffects();

        COMOTOrchestrator orchestrator = new COMOTOrchestrator().withSalsaIP("128.130.172.215").withSalsaPort(8380).withRsyblIP("128.130.172.215").withRsyblPort(8280);

        orchestrator.deploy(serviceTemplate);
    }
}
