package at.ac.tuwien.dsg.comot.orchestrator;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifact;
import static at.ac.tuwien.dsg.comot.common.model.BASHAction.BASHAction;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall;
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
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.FlexiantSmall;
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
public class ElasticIoTPlatform_Cost_Only_LoadBalancer {

    public static void main(String[] args) {
        //specify service units in terms of software

        String salsaRepo = "http://109.231.121.57/EventProcessingNoDB/";

        //finally, we define Vm types for event processing
        final ServiceUnit loadbalancerVM = OperatingSystemUnit("LoadBalancerUnitVM")
                .providedBy(FlexiantSmall()
                        .withBaseImage("4a73a8db-640d-3c2a-a52c-ff1f447365aa")
                );

        //add the service units belonging to the event processing topology
        final ServiceUnit loadbalancerUnit = SingleSoftwareUnit("LoadBalancerUnit")
                //load balancer must provide IP
                .exposes(Capability.Variable("LoadBalancer_IP_information"))
                .deployedBy(SingleScriptArtifact("deployLoadBalancerArtifact", salsaRepo + "deployLoadBalancer.sh"))
                .withMaxColocatedInstances(1);

        final ServiceTopology eventProcessingTopology = ServiceTopology("EventProcessingTopology")
                .withServiceUnits(loadbalancerUnit, loadbalancerVM
                );

        final COMOTOrchestrator orchestrator = new COMOTOrchestrator().withSalsaIP("109.231.121.57").withSalsaPort(8380).withRsyblIP("128.130.172.215").withRsyblPort(8280);

        CloudService serviceTemplate = ServiceTemplate("EventProcessingTopology_COMMON2")
                .consistsOfTopologies(eventProcessingTopology)
                //                .consistsOfTopologies(localProcessinTopology)
                //defining CONNECT_TO and HOSTED_ON relationships
                .andRelationships(
                        //event processing gets IP from load balancer
                        HostedOnRelation("loadbalancerToVM")
                        .from(loadbalancerUnit)
                        .to(loadbalancerVM)
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
}
