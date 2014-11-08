package at.ac.tuwien.dsg.comot.core;

import static at.ac.tuwien.dsg.comot.common.fluent.ArtifactTemplate.SingleScriptArtifactTemplate;
import static at.ac.tuwien.dsg.comot.common.fluent.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.fluent.CommonOperatingSystemSpecification.OpenstackSmall;
import static at.ac.tuwien.dsg.comot.common.fluent.EntityRelationship.ConnectToRelation;
import static at.ac.tuwien.dsg.comot.common.fluent.EntityRelationship.HostedOnRelation;
import static at.ac.tuwien.dsg.comot.common.fluent.OperatingSystemUnit.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.fluent.ServiceTopology.ServiceTopology;
import static at.ac.tuwien.dsg.comot.common.fluent.SoftwareNode.SingleSoftwareUnit;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.fluent.Capability;
import at.ac.tuwien.dsg.comot.common.fluent.CloudService;
import at.ac.tuwien.dsg.comot.common.fluent.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.fluent.Requirement;
import at.ac.tuwien.dsg.comot.common.fluent.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.fluent.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.test.TestUtils;
import at.ac.tuwien.dsg.comot.core.test.AbstractTest;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;


/**
 *
 * @author http://dsg.tuwien.ac.at
 */
public class ProgrammingAndControllingElasticityWithCOMOT_GXG extends AbstractTest{

	@Test
	public void testStuff() throws JAXBException, IOException, CoreServiceException {
        //specify service units in terms of software

        //need to specify details of VM and operating system to deploy the software servide units on
        OperatingSystemUnit mqtt_brokerVM = OperatingSystemUnit("mqtt_brokerVM")
                .providedBy(OpenstackSmall("OpenStackSmall_OS_DC")
                        .withProvider("dsg@openstack")
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );


        //finally, we define Vm types for event processing
        OperatingSystemUnit sensorVM = OperatingSystemUnit("sensorVM")
                .providedBy(OpenstackSmall("OpenStackSmall_OS_LB")
                        .withProvider("dsg@openstack")
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );

        OperatingSystemUnit processingVM = OperatingSystemUnit("processingVM")
                .providedBy(OpenstackSmall("OpenStackSmall_OS_LB")
                        .withProvider("dsg@openstack")
                        .addSoftwarePackage("openjdk-7-jre")
                        .addSoftwarePackage("ganglia-monitor")
                        .addSoftwarePackage("gmetad")
                );

        //start with Data End, and first with Data Controller
        ServiceUnit mqtt_brokerUnit = SingleSoftwareUnit("mqtt_broker")
                //software artifacts needed for unit deployment   = script to deploy Cassandra
                .deployedBy(SingleScriptArtifactTemplate("deployArtifactMQTT", "/IoT/run_mqtt_broker.sh"))
                //data controller exposed its IP 
                .exposes(Capability.Variable("brokerIp_Capability"));

        
        
        //start with Data End, and first with Data Controller
        ServiceUnit processingUnit = SingleSoftwareUnit("processing")
                //software artifacts needed for unit deployment   = script to deploy Cassandra
                .deployedBy(SingleScriptArtifactTemplate("deployArtifactProcessing", "/IoT/install-local-analysis-service.sh"))
                //data controller exposed its IP 
                .requires(Requirement.Variable("brokerIp_Requirement"));

        
        
        //start with Data End, and first with Data Controller
        ServiceUnit sensorUnit = SingleSoftwareUnit("sensor")
                //software artifacts needed for unit deployment   = script to deploy Cassandra
                .deployedBy(SingleScriptArtifactTemplate("deployArtifactSensor", "/IoT/run_sensor.sh"))
                //data controller exposed its IP 
                 .requires(Requirement.Variable("brokerIp_Requirement_2"));

        
        
       
        //Describe a Data End service topology containing the previous 2 software service units
        ServiceTopology serviceTopology = ServiceTopology("ServiceTopology")
                .withServiceUnits( mqtt_brokerUnit, mqtt_brokerVM, processingUnit,processingVM, sensorUnit, sensorVM
                );

        
        //describe the service template which will hold more topologies
        CloudService serviceTemplate = ServiceTemplate("DaasService")
                .consistsOfTopologies(serviceTopology)
                
                //defining CONNECT_TO and HOSTED_ON relationships
                .andRelationships(
                        //Data Controller IP send to Data Node
                        ConnectToRelation("sensor_to_broker")
                        .from(mqtt_brokerUnit.getContext().get("brokerIp_Capability"))
                        .to(processingUnit.getContext().get("brokerIp_Requirement_2")) //specify which software unit goes to which VM
                        ,
                        //event processing gets IP from load balancer
                        ConnectToRelation("b")
                        .from(mqtt_brokerUnit.getContext().get("brokerIp_Capability"))
                        .to(sensorUnit.getContext().get("brokerIp_Requirement")) //specify which software unit goes to which VM
                        ,
                        HostedOnRelation("d")
                        .from(mqtt_brokerUnit)
                        .to(mqtt_brokerVM),
                        HostedOnRelation("e")
                        .from(processingUnit)
                        .to(processingVM) //add hosted on relatinos
                        , HostedOnRelation("f")
                        .from(sensorUnit)
                        .to(sensorVM)
                )
                // as we have horizontally scalable distributed systems (one service unit can have more instances)
                //metrics must be aggregated among VMs
                .withDefaultMetrics()
                //to find scaling actions, one must assume some effects for each action, to understand
                //if it makes sense or not to execute the action
                .withDefaultActionEffects()
                
                ;
        
                //instantiate COMOT orchestrator to deploy, monitor and control the service
        orchestrator 
                        //we have SALSA as cloud management tool
                        //curently deployed separately
                        .withSalsaIP("128.130.172.215")
                        .withSalsaPort(8080)
                        
                        //we have rSYBL elasticity control service and MELA 
                        //deployed separately
                        .withRsyblIP("128.130.172.214")
                        .withRsyblPort(8081);
                
                //deploy, monitor and control
        CompositionRulesConfiguration rules = TestUtils.loadMetricCompositionRules(serviceTemplate.getId(),
				serviceTemplate.getMetricCompositonRulesFile());

		String effects = TestUtils.loadFile(serviceTemplate.getEffectsCompositonRulesFile());

		orchestrator.deployAndControl(serviceTemplate, rules, effects);
//                orchestrator.controlExisting(serviceTemplate);
                
    }
}
