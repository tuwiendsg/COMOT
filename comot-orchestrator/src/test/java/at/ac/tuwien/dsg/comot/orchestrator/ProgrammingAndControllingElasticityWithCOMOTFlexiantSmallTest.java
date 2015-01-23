package at.ac.tuwien.dsg.comot.orchestrator;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifact;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.SingleSoftwareUnit;
import at.ac.tuwien.dsg.comot.orchestrator.interraction.COMOTOrchestrator;

/**
 *
 * @author http://dsg.tuwien.ac.at
 */
public class ProgrammingAndControllingElasticityWithCOMOTFlexiantSmallTest {

    public static void main(String[] args) {
        //specify service units in terms of software

        String salsaRepo = "http://128.130.172.215/salsa/upload/files/DaaS_Flexiant/";

        

        //finally, we define Vm types for event processing
        OperatingSystemUnit loadbalancerVM = OperatingSystemUnit("LoadBalancerUnitVM")
                .providedBy(CommonOperatingSystemSpecification.FlexiantSmall()
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad"));
         
        //add the service units belonging to the event processing topology
        ServiceUnit loadbalancerUnit = SingleSoftwareUnit("LoadBalancerUnit")
                //load balancer must provide IP
                .exposes(Capability.Variable("LoadBalancer_IP_information"))
                .deployedBy(SingleScriptArtifact("deployLoadBalancerArtifact", salsaRepo + "deployLoadBalancer.sh"));

        
        //define event processing unit topology
        ServiceTopology eventProcessingTopology = ServiceTopology("EventProcessingTopology")
                .withServiceUnits(loadbalancerUnit 
                        , loadbalancerVM 
                );

        

        //describe the service template which will hold more topologies
        CloudService serviceTemplate = ServiceTemplate("Testsss")
              
                .consistsOfTopologies(eventProcessingTopology)
                //defining CONNECT_TO and HOSTED_ON relationships
                .andRelationships(
                         
                         HostedOnRelation("loadbalancerToVM")
                        .from(loadbalancerUnit)
                        .to(loadbalancerVM) 
                        
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
                .withSalsaIP("109.231.122.193")
//                .withSalsaIP("128.130.172.215")
                .withSalsaPort(8380)
                //we have rSYBL elasticity control service and MELA 
                //deployed separately
                //                .withRsyblIP("109.231.121.26")
                //                .withRsyblIP("localhost")
                //                                .withRsyblIP("109.231.121.104")
//                .withRsyblIP("128.130.172.214")
                //                .withRsyblIP("128.131.172.4118")
                .withRsyblPort(8280);
//                .withRsyblPort(8080);

        //deploy, monitor and control
//        orchestrator.deployAndControl(serviceTemplate);
//        orchestrator.deploy(serviceTemplate);
        orchestrator.deploy(serviceTemplate);

    }
}
