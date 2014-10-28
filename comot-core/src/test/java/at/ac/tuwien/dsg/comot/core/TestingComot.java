/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.core;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifactTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackMicro;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.ConnectToRelation;
import static at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;
import static at.ac.tuwien.dsg.comot.common.model.SoftwareNode.SingleSoftwareUnit;
import static at.ac.tuwien.dsg.comot.common.model.Strategy.Strategy;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import at.ac.tuwien.dsg.comot.client.test.TestUtils;
import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.common.model.Constraint;
import at.ac.tuwien.dsg.comot.common.model.Constraint.Metric;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.Requirement;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.Strategy;
import at.ac.tuwien.dsg.comot.core.test.AbstractTest;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

/**
 * ======= import static
 * at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifactTemplate;
 * import at.ac.tuwien.dsg.comot.common.model.Capability; import static
 * at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackMicro;
 * import static
 * at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackSmall;
 * import at.ac.tuwien.dsg.comot.common.model.Constraint; import
 * at.ac.tuwien.dsg.comot.common.model.Constraint.Metric; import static
 * at.ac.tuwien.dsg.comot.common.model.EntityRelationship.ConnectToRelation;
 * import static
 * at.ac.tuwien.dsg.comot.common.model.EntityRelationship.HostedOnRelation;
 * import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit; import static
 * at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
 * import at.ac.tuwien.dsg.comot.common.model.Requirement; import
 * at.ac.tuwien.dsg.comot.common.model.ServiceTemplate; import
 * at.ac.tuwien.dsg.comot.common.model.ServiceTopology; import static
 * at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology; import
 * at.ac.tuwien.dsg.comot.common.model.ServiceUnit; import static
 * at.ac.tuwien.dsg.comot.common.model.SoftwareNode.SingleSoftwareUnit; import
 * at.ac.tuwien.dsg.comot.common.model.Strategy; import static
 * at.ac.tuwien.dsg.comot.common.model.Strategy.Strategy; import
 * at.ac.tuwien.dsg.orchestrator.interraction.COMOTOrchestrator;
 *
 * /**
 *
 * >>>>>>> comot-orchestrator
 *
 * @author Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at
 */
public class TestingComot extends AbstractTest{

	@Test
	public void testStuff() throws JAXBException, IOException, CoreServiceException {

        ServiceUnit dataControllerUnit = SingleSoftwareUnit("DataControllerUnit")
                .deployedBy(SingleScriptArtifactTemplate("deployCassandraSeedArtifact", "deployCassandraSeed.sh"))
                .exposes(Capability.Variable("DataControllerCapability_IP"));

        ServiceUnit loadBalancerUnit = SingleSoftwareUnit("LoadBalancerUnit")
                .deployedBy(SingleScriptArtifactTemplate("deployLoadBalancerArtifact", "deployLoadBalancer.sh"))
                .exposes(Capability.Variable("LoadBalancerCapability_IP"));

        ServiceUnit dataNodeUnit = SingleSoftwareUnit("DataNodeUnit")
                .deployedBy(SingleScriptArtifactTemplate("artifactID", "artifact"))
                .requires(Requirement.Variable("dataControllerIP"))
                .controlledBy(Strategy("ST1")
                        .when(Constraint.MetricConstraint("ST1CO1",
                                        new Metric("metric", "unit")).lessThan("value"))
                        .then(Strategy.Action.ScaleIn));

        OperatingSystemUnit dataNodeOS = OperatingSystemUnit("DataNodeVM")
                .providedBy(OpenstackMicro("OS_DataNode_Small")
                        .withProvider("dsg@openstack")
                        .addSoftwarePackage("openjdk-7-jre")
                );

        ServiceUnit eventProcessingUnit = SingleSoftwareUnit("EventProcessingUnit")
                .deployedBy(SingleScriptArtifactTemplate("deployEventProcessingArtifact", "deployEventProcessing.sh"))
                .requires(Requirement.Variable("LoadBalancer_IP_req_eventProcessing").withName("Load Balancer IP"))
                .requires(Requirement.Variable("DataController_IP_req_eventProcessing").withName("Data Controller IP"))
                .controlledBy(Strategy("ST2")
                        .when(Constraint.MetricConstraint("ST2CO1", new Metric("responseTime", "ms")).lessThan("200"))
                        .and(Constraint.MetricConstraint("ST2CO2", new Metric("throughput", "operations/s")).lessThan("200"))
                        .then(Strategy.Action.ScaleIn));

        OperatingSystemUnit dataControllerOS = OperatingSystemUnit.OperatingSystemUnit("DataControllerVM")
                .providedBy(OpenstackSmall("OS_DataController_Small")
                        .withProvider("dsg@openstack")
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );

        OperatingSystemUnit loadBalancerrOS = OperatingSystemUnit.OperatingSystemUnit("DLoadbalancerVM")
                .providedBy(OpenstackSmall("OS_LoadBalancer_Small")
                        .withProvider("dsg@openstack")
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );

        OperatingSystemUnit eventProcessingOS = OperatingSystemUnit.OperatingSystemUnit("EventProcessingVM")
                .providedBy(OpenstackMicro("OS_EventProcessing_Micro")
                        .withProvider("dsg@openstack")
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );

        ServiceTopology dataEndTopology = ServiceTopology("DataEndTopology")
                .withServiceUnits(dataControllerUnit, dataNodeUnit,
                        dataControllerOS, dataNodeOS);

        dataEndTopology.constrainedBy(
                Constraint.MetricConstraint("DataEnd_CO1",
                        new Metric("metruic", "unit")).lessThan("value"));

        ServiceTopology eventProcessingTopology = ServiceTopology.ServiceTopology("EventProcessingTopology")
                .withServiceUnits(eventProcessingUnit, loadBalancerUnit, loadBalancerrOS, eventProcessingOS);

        eventProcessingTopology.constrainedBy(Constraint.MetricConstraint("C02", new Metric("responseTime", "ms")).lessThan("600"));

        CloudService dataService = CloudService.ServiceTemplate("DaasService")
                .consistsOfTopologies(dataEndTopology)
                .consistsOfTopologies(eventProcessingTopology)
                .andRelationships(
                        HostedOnRelation("dataControllerHostedOnVM")
                        .from(dataControllerUnit)
                        .to(dataControllerOS),
                        ConnectToRelation("dataNodeToDataController")
                        .from(dataControllerUnit.getContext().get("DataControllerCapability_IP"))
                        .to(dataNodeUnit.getContext().get("DataController_IP_req_dataNode"))
                //                      ...
                )
                .withDefaultMetrics()
                .withDefaultActionEffects();
        ;

       
    	CompositionRulesConfiguration rules = TestUtils.loadMetricCompositionRules(dataService.getId(),
    			dataService.getMetricCompositonRulesFile());
		
		String effects = TestUtils.loadFile(dataService.getEffectsCompositonRulesFile());
		
		orchestrator.deployAndControl(dataService, rules, effects);

    }
}
