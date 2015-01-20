package at.ac.tuwien.dsg.comot.orchestrator;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifact;
import static at.ac.tuwien.dsg.comot.common.model.BASHAction.BASHAction;
import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.DockerDefault;
import static at.ac.tuwien.dsg.comot.common.model.DockerUnit.DockerUnit;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.ConnectToRelation;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.SingleSoftwareUnit;
import static at.ac.tuwien.dsg.comot.common.model.Strategy.Strategy;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.LocalDocker;
import at.ac.tuwien.dsg.comot.common.model.Constraint;
import at.ac.tuwien.dsg.comot.common.model.Constraint.Metric;
import at.ac.tuwien.dsg.comot.common.model.DockerUnit;
import at.ac.tuwien.dsg.comot.common.model.ElasticityCapability;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.LifecyclePhase;
import at.ac.tuwien.dsg.comot.common.model.Requirement;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import at.ac.tuwien.dsg.orchestrator.interraction.COMOTOrchestrator;

/**
 *
 * @author http://dsg.tuwien.ac.at
 */
public class ComotHelloElasticity {

    public static void main(String[] args) {
        //specify service units in terms of software

        String salsaRepo = "http://128.130.172.215/repository/files/HelloElasticity/";
        
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

        //elasticity capabilities as first class citizens
        ElasticityCapability scaleOutEventProcessing = ElasticityCapability.ScaleOut();
        ElasticityCapability scaleInEventProcessing = ElasticityCapability.ScaleIn();

        //add the service units belonging to the event processing topology
        ServiceUnit eventProcessingUnit = SingleSoftwareUnit("EventProcessingUnit")
                .deployedBy(SingleScriptArtifact(salsaRepo + "deployEventProcessing.sh"))
                //enabled elasticity capabilities
                .provides(scaleOutEventProcessing, scaleInEventProcessing)
                //event processing must register in Load Balancer, so it needs the IP
                .requires(Requirement.Variable("EventProcessingUnit_LoadBalancer_IP_Req"))
                //event processing also needs to querry the Data Controller to access data
                //scale IN if throughput < 200 and responseTime < 200
                .controlledBy(
                        Strategy("EP_ST1")
                        .when(Constraint.MetricConstraint("EP_ST1_CO1", new Metric("responseTime", "ms")).lessThan("500"))
                        .and(Constraint.MetricConstraint("EP_ST1_CO2", new Metric("totalPendingRequests", "#")).lessThan("10"))
                        .enforce(scaleInEventProcessing),
                        Strategy("EP_ST2")
                        .when(Constraint.MetricConstraint("EP_ST1_CO1", new Metric("responseTime", "ms")).greaterThan("1000"))
                        .and(Constraint.MetricConstraint("EP_ST1_CO2", new Metric("totalPendingRequests", "#")).greaterThan("10"))
                        .enforce(scaleOutEventProcessing)
                )
                //manage unit lifecycle
                .withLifecycleAction(LifecyclePhase.STOP, BASHAction("sudo service event-processing stop"));

        //add the service units belonging to the event processing topology
        ServiceUnit loadbalancerUnit = SingleSoftwareUnit("LoadBalancerUnit")
                //load balancer must provide IP
                .exposes(Capability.Variable("LoadBalancer_IP_information"))
                .deployedBy(SingleScriptArtifact(salsaRepo + "deployLoadBalancer.sh"));

        //define event processing unit topology
        ServiceTopology eventProcessingTopology = ServiceTopology("EventProcessingTopology")
                .withServiceUnits(loadbalancerUnit, eventProcessingUnit //add vm types to topology
                        , loadbalancerDocker, eventProcessingDocker, privateVM
                );

        //describe the service template which will hold more topologies
        CloudService serviceTemplate = ServiceTemplate("HelloElasticity")
                .consistsOfTopologies(eventProcessingTopology)
                //defining CONNECT_TO and HOSTED_ON relationships
                .andRelationships(
                        //event processing receives IP from load balancer
                        ConnectToRelation("eventProcessingToLoadBalancer")
                        .from(loadbalancerUnit.getContext().get("LoadBalancer_IP_information"))
                        .to(eventProcessingUnit.getContext().get("EventProcessingUnit_LoadBalancer_IP_Req")),
                        //specify which software unit goes to which virtual container
                        HostedOnRelation("loadbalancerToDocker")
                        .from(loadbalancerUnit)
                        .to(loadbalancerDocker),
                        HostedOnRelation("loadbalancerDockerToLocal")
                        .from(loadbalancerDocker)
                        .to(privateVM),
                        HostedOnRelation("eventProcessingToDocker")
                        .from(eventProcessingUnit)
                        .to(eventProcessingDocker),
                        HostedOnRelation("eventProcessingDockerToVM")
                        .from(eventProcessingDocker)
                        .to(privateVM)
                )
                // as we have horizontally scalable distributed systems (one service unit can have more instances)
                //metrics must be aggregated among virtual containers
                .withDefaultMetrics();

        //instantiate COMOT orchestrator to deploy, monitor and control the service
        COMOTOrchestrator orchestrator = new COMOTOrchestrator("128.130.172.214");

        orchestrator.deployAndControl( serviceTemplate);
    }
}
