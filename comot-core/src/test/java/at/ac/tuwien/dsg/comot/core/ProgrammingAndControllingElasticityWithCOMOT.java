package at.ac.tuwien.dsg.comot.core;

import static at.ac.tuwien.dsg.comot.common.fluent.ArtifactTemplate.SingleScriptArtifactTemplate;
import static at.ac.tuwien.dsg.comot.common.fluent.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.fluent.CommonOperatingSystemSpecification.OpenstackMicro;
import static at.ac.tuwien.dsg.comot.common.fluent.CommonOperatingSystemSpecification.OpenstackSmall;
import static at.ac.tuwien.dsg.comot.common.fluent.EntityRelationship.ConnectToRelation;
import static at.ac.tuwien.dsg.comot.common.fluent.EntityRelationship.HostedOnRelation;
import static at.ac.tuwien.dsg.comot.common.fluent.OperatingSystemUnit.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.fluent.ServiceTopology.ServiceTopology;
import static at.ac.tuwien.dsg.comot.common.fluent.SoftwareNode.SingleSoftwareUnit;
import static at.ac.tuwien.dsg.comot.common.fluent.Strategy.Strategy;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import at.ac.tuwien.dsg.comot.client.test.TestUtils;
import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.fluent.Capability;
import at.ac.tuwien.dsg.comot.common.fluent.CloudService;
import at.ac.tuwien.dsg.comot.common.fluent.Constraint;
import at.ac.tuwien.dsg.comot.common.fluent.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.fluent.Requirement;
import at.ac.tuwien.dsg.comot.common.fluent.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.fluent.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.fluent.Strategy;
import at.ac.tuwien.dsg.comot.common.fluent.Constraint.Metric;
import at.ac.tuwien.dsg.comot.common.test.samples.DataAsAServiceCloudApplication;
import at.ac.tuwien.dsg.comot.core.test.AbstractTest;
import at.ac.tuwien.dsg.comot.cs.transformer.ToscaDescriptionBuilderImpl;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

/**
 *
 * @author http://dsg.tuwien.ac.at
 */
public class ProgrammingAndControllingElasticityWithCOMOT extends AbstractTest{

	@Test
	public void testStuff() throws CoreServiceException, JAXBException, IOException {
		// specify service units in terms of software

		// need to specify details of VM and operating system to deploy the software servide units on
		OperatingSystemUnit dataControllerVM = OperatingSystemUnit("DataControllerUnitVM")
				.providedBy(OpenstackSmall("OpenStackSmall_OS_DC")
						.addSoftwarePackage("openjdk-7-jre")
						.addSoftwarePackage("ganglia-monitor")
						.addSoftwarePackage("gmetad")
				);

		OperatingSystemUnit dataNodeVM = OperatingSystemUnit("DataNodeUnitVM")
				.providedBy(OpenstackMicro("OpenStackMicro_OS_DN")
						.addSoftwarePackage("openjdk-7-jre")
						.addSoftwarePackage("ganglia-monitor")
						.addSoftwarePackage("gmetad")
				);

		// finally, we define Vm types for event processing
		OperatingSystemUnit loadbalancerVM = OperatingSystemUnit("LoadBalancerUnitVM")
				.providedBy(OpenstackSmall("OpenStackSmall_OS_LB")
						.addSoftwarePackage("openjdk-7-jre")
						.addSoftwarePackage("ganglia-monitor")
						.addSoftwarePackage("gmetad")
				);

		OperatingSystemUnit eventProcessingVM = OperatingSystemUnit("EventProcessingUnitVM")
				.providedBy(OpenstackSmall("OpenStackMicro_OS_EP")
						.withBaseImage("be6ae07b-7deb-4926-bfd7-b11afe228d6a")
						.addSoftwarePackage("openjdk-7-jre")
						.addSoftwarePackage("ganglia-monitor")
						.addSoftwarePackage("gmetad")
				);

		OperatingSystemUnit localProcessingVM = OperatingSystemUnit("LocalProcessingUnitVM")
				.providedBy(OpenstackSmall("OpenStackSmall_OS_LP")
						.withBaseImage("be6ae07b-7deb-4926-bfd7-b11afe228d6a")
						.addSoftwarePackage("openjdk-7-jre")
						.addSoftwarePackage("ganglia-monitor")
						.addSoftwarePackage("gmetad")
				);

		OperatingSystemUnit mqttQueueVM = OperatingSystemUnit("MqttQueueVM")
				.providedBy(OpenstackSmall("OpenStackSmall_OS_MQTT")
						.withBaseImage("17ffd200-315f-4ba8-9e77-c294efc772bd")
						.addSoftwarePackage("openjdk-7-jre")
						.addSoftwarePackage("ganglia-monitor")
						.addSoftwarePackage("gmetad")
				);

		OperatingSystemUnit momVM = OperatingSystemUnit("MoMVM")
				.providedBy(OpenstackSmall("OpenStackSmall_OS_MOM")
						.withBaseImage("be6ae07b-7deb-4926-bfd7-b11afe228d6a")
						.addSoftwarePackage("openjdk-7-jre")
						.addSoftwarePackage("ganglia-monitor")
						.addSoftwarePackage("gmetad")
				);

		// start with Data End, and first with Data Controller
		ServiceUnit dataControllerUnit = SingleSoftwareUnit("DataControllerUnit")
				// software artifacts needed for unit deployment = script to deploy Cassandra
				.deployedBy(
						SingleScriptArtifactTemplate("deployDataControllerArtifact",
								"http://128.130.172.215/salsa/upload/files/DaasService/deployCassandraSeed.sh"))
				// data controller exposed its IP
				.exposes(Capability.Variable("DataController_IP_information"));

		// specify data node
		ServiceUnit dataNodeUnit = SingleSoftwareUnit("DataNodeUnit")
				.deployedBy(
						SingleScriptArtifactTemplate("deployDataNodeArtifact",
								"http://128.130.172.215/salsa/upload/files/DaasService/deployCassandraNode.sh"))
				// data node MUST KNOW the IP of cassandra seed, to connect to it and join data cluster
				.requires(Requirement.Variable("DataController_IP_Data_Node_Req").withName("requiringDataNodeIP"))
				// express elasticity strategy: Scale IN Data Node when cpu usage < 40%
				.controlledBy(Strategy("ST1")
						.when(Constraint.MetricConstraint("ST1CO1", new Metric("cpuUsage", "%")).lessThan("40"))
						.then(Strategy.Action.ScaleIn)
				);

		// add the service units belonging to the event processing topology
		ServiceUnit momUnit = SingleSoftwareUnit("MOM")
				// load balancer must provide IP
				.exposes(Capability.Variable("MOM_IP_information"))
				.deployedBy(
						SingleScriptArtifactTemplate("deployMOMArtifact",
								"http://128.130.172.215/salsa/upload/files/DaasService/deployQueue.sh"));

		// add the service units belonging to the event processing topology
		ServiceUnit eventProcessingUnit = SingleSoftwareUnit("EventProcessingUnit")
				.deployedBy(
						SingleScriptArtifactTemplate("deployEventProcessingArtifact",
								"http://128.130.172.215/salsa/upload/files/DaasService/deployEventProcessing.sh"))
				// event processing must register in Load Balancer, so it needs the IP
				.requires(Requirement.Variable("EventProcessingUnit_LoadBalancer_IP_Req"))
				// event processing also needs to querry the Data Controller to access data
				.requires(Requirement.Variable("EventProcessingUnit_DataController_IP_Req"))
				.requires(Requirement.Variable("EventProcessingUnit_MOM_IP_Req"))
				// scale IN if throughput < 200 and responseTime < 200
				.controlledBy(
						Strategy("ST2")
								.when(Constraint.MetricConstraint("ST2CO1", new Metric("responseTime", "ms")).lessThan(
										"200"))
								.and(Constraint.MetricConstraint("ST2CO2", new Metric("throughput", "operations/s"))
										.lessThan("200"))
								.then(Strategy.Action.ScaleIn)
				);

		// add the service units belonging to the event processing topology
		ServiceUnit loadbalancerUnit = SingleSoftwareUnit("LoadBalancerUnit")
				// load balancer must provide IP
				.exposes(Capability.Variable("LoadBalancer_IP_information"))
				.deployedBy(
						SingleScriptArtifactTemplate("deployLoadBalancerArtifact",
								"http://128.130.172.215/salsa/upload/files/DaasService/deployLoadBalancer.sh"));

		ServiceUnit mqttUnit = SingleSoftwareUnit("QueueUnit")
				// load balancer must provide IP
				.exposes(Capability.Variable("brokerIp_Capability"))
				.deployedBy(
						SingleScriptArtifactTemplate("deployMQTTBroker",
								"http://128.130.172.215/salsa/upload/files/DaasService/IoT/run_mqtt_broker.sh"));

		ServiceUnit localProcessingUnit = SingleSoftwareUnit("LocalProcessingUnit")
				// load balancer must provide IP
				.requires(Requirement.Variable("brokerIp_Requirement"))
				.requires(Requirement.Variable("daasIp_Requirement"))
				.deployedBy(
						SingleScriptArtifactTemplate("deployLocalProcessing",
								"http://128.130.172.215/salsa/upload/files/DaasService/IoT/install-local-analysis-service.sh"));

		// Describe a Data End service topology containing the previous 2 software service units
		ServiceTopology dataEndTopology = ServiceTopology("DataEndTopology")
				.withServiceUnits(dataControllerUnit, dataNodeUnit // add also OS units to topology
						, dataControllerVM, dataNodeVM
				);

		// specify constraints on the data topology
		// thus, the CPU usage of all Service Unit instances of the data end Topology must be below 80%
		dataEndTopology.constrainedBy(Constraint.MetricConstraint("DataEndCO1", new Metric("cpuUsage", "%")).lessThan(
				"40"));

		// define event processing unit topology
		ServiceTopology eventProcessingTopology = ServiceTopology("EventProcessingTopology")
				.withServiceUnits(loadbalancerUnit, eventProcessingUnit, momUnit // add vm types to topology
						, loadbalancerVM, eventProcessingVM, momVM
				);

		ServiceTopology localProcessinTopology = ServiceTopology("LocalProcessingTopology")
				.withServiceUnits(mqttQueueVM, mqttUnit, localProcessingUnit, localProcessingVM
				);

		eventProcessingTopology.constrainedBy(Constraint.MetricConstraint("C02", new Metric("responseTime", "ms"))
				.lessThan("400"));

		// describe the service template which will hold more topologies
		CloudService serviceTemplate = ServiceTemplate("test_comot")
				.consistsOfTopologies(dataEndTopology)
				.consistsOfTopologies(eventProcessingTopology)
				.consistsOfTopologies(localProcessinTopology)
				// defining CONNECT_TO and HOSTED_ON relationships
				.andRelationships(
						// Data Controller IP send to Data Node
						ConnectToRelation("dataNodeToDataController")
								.from(dataControllerUnit.getContext().get("DataController_IP_information"))
								.to(dataNodeUnit.getContext().get("DataController_IP_Data_Node_Req")) // specify which
																										// software unit
																										// goes to which
																										// VM
						,
						// event processing gets IP from load balancer
						ConnectToRelation("eventProcessingToLoadBalancer")
								.from(loadbalancerUnit.getContext().get("LoadBalancer_IP_information"))
								.to(eventProcessingUnit.getContext().get("EventProcessingUnit_LoadBalancer_IP_Req")) // specify
																														// which
																														// software
																														// unit
																														// goes
																														// to
																														// which
																														// VM
						,
						// event processing gets IP from data controller
						ConnectToRelation("eventProcessingToDataController")
								.from(dataControllerUnit.getContext().get("DataController_IP_information"))
								.to(eventProcessingUnit.getContext().get("EventProcessingUnit_DataController_IP_Req")) // specify
																														// which
																														// software
																														// unit
																														// goes
																														// to
																														// which
																														// VM
						,
						ConnectToRelation("eventProcessingToMOM")
								.from(momUnit.getContext().get("MOM_IP_information"))
								.to(eventProcessingUnit.getContext().get("EventProcessingUnit_MOM_IP_Req")) // specify
																											// which
																											// software
																											// unit goes
																											// to which
																											// VM
						,
						ConnectToRelation("queue_to_DaaS")
								.from(loadbalancerUnit.getContext().get("LoadBalancer_IP_information"))
								.to(eventProcessingUnit.getContext().get("daasIp_Requirement")) // specify which
																								// software unit goes to
																								// which VM
						,
						ConnectToRelation("mqtt_broker")
								.from(loadbalancerUnit.getContext().get("brokerIp_Capability"))
								.to(eventProcessingUnit.getContext().get("brokerIp_Requirement")) // specify which
																									// software unit
																									// goes to which VM
						,
						HostedOnRelation("dataControllerToVM")
								.from(dataControllerUnit)
								.to(dataControllerVM),
						HostedOnRelation("dataNodeToVM")
								.from(dataNodeUnit)
								.to(dataNodeVM) // add hosted on relatinos
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
				// metrics must be aggregated among VMs
				.withDefaultMetrics()
				// to find scaling actions, one must assume some effects for each action, to understand
				// if it makes sense or not to execute the action
				.withDefaultActionEffects();

		// instantiate COMOT orchestrator to deploy, monitor and control the service
		orchestrator 
				// we have SALSA as cloud management tool
				// curently deployed separately
				.withSalsaIP("128.130.172.215")
				.withSalsaPort(8080)
				// we have rSYBL elasticity control service and MELA
				// deployed separately
				// .withRsyblIP("128.130.172.214")
				.withRsyblIP("localhost")
				// .withRsyblIP("109.231.121.66")
				.withRsyblPort(8280);

		// deploy, monitor and control
		// orchestrator.deployAndControl(serviceTemplate);
		CompositionRulesConfiguration rules = TestUtils.loadMetricCompositionRules(serviceTemplate.getId(),
				serviceTemplate.getMetricCompositonRulesFile());

		String effects = TestUtils.loadFile(serviceTemplate.getEffectsCompositonRulesFile());

		String xml = new ToscaDescriptionBuilderImpl().toXml(serviceTemplate);
		log.info(xml);
		
		
		//orchestrator.deployAndControl(serviceTemplate, rules, effects);
		// orchestrator.controlExisting(serviceTemplate);

		// DefaultSalsaClient salsa = new DefaultSalsaClient();
		// String xml = salsa.getToscaDescriptionBuilder().toXml(serviceTemplate);
		// System.out.print(xml);
		// System.out.print("DONE !");

	}
}
