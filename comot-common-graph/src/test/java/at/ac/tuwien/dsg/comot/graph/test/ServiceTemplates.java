package at.ac.tuwien.dsg.comot.graph.test;

import java.util.HashSet;
import java.util.Set;

import at.ac.tuwien.dsg.comot.graph.model.ConnectToRelationship;
import at.ac.tuwien.dsg.comot.graph.model.SyblDirective;
import at.ac.tuwien.dsg.comot.graph.model.node.ArtifactReference;
import at.ac.tuwien.dsg.comot.graph.model.node.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.graph.model.node.NodeInstance;
import at.ac.tuwien.dsg.comot.graph.model.node.NodeInstanceOs;
import at.ac.tuwien.dsg.comot.graph.model.node.Properties;
import at.ac.tuwien.dsg.comot.graph.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.graph.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.graph.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.graph.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.graph.model.type.ArtifactType;
import at.ac.tuwien.dsg.comot.graph.model.type.DirectiveType;
import at.ac.tuwien.dsg.comot.graph.model.type.NodePropertiesType;
import at.ac.tuwien.dsg.comot.graph.model.type.NodeType;
import at.ac.tuwien.dsg.comot.graph.model.type.State;

public class ServiceTemplates {

	public static final String swNodeId = "nodeId";
	public static final String swNodeId2 = "nodeId2";
	public static final String osNodeId = "osId";
	public static final String topologyId = "topologyId";
	public static final String serviceId = "serviceId";

	public static CloudService simplifiedService() {

		// NODE OS
		Properties properties = new Properties(NodePropertiesType.OS);
		properties.addProperty("instanceType", "000000512");
		properties.addProperty("provider", "dsg@openstack");
		properties.setId("propertiesId");

		StackNode osNode = new StackNode(osNodeId, "Test os", 1, 2, NodeType.OS);
		osNode.addProperties(properties);

		// NODE SW 1
		StackNode swNode = new StackNode(swNodeId, "Test node unit", 2, 5, NodeType.SOFTWARE);
		swNode.setHostNode(osNode);

		// NODE SW 2
		StackNode swNode2 = new StackNode(swNodeId2, "Test node unit 2", 2, 5, NodeType.SOFTWARE);
		swNode2.setHostNode(osNode);

		// CONNECT TO
		ConnectToRelationship rel = new ConnectToRelationship("connectVariableName", swNode2, swNode);
		swNode2.addConnectTo(rel);

		// UNITS
		ServiceUnit unit = new ServiceUnit(swNode);
		ServiceUnit unit2 = new ServiceUnit(swNode2);

		// TOPOLOGY
		ServiceTopology topology = new ServiceTopology(topologyId);
		topology.addNode(swNode);
		topology.addNode(osNode);
		topology.addNode(swNode2);
		topology.addServiceUnit(unit);
		topology.addServiceUnit(unit2);

		CloudService service = new CloudService(serviceId);
		service.addServiceTopology(topology);
		return service;
	}
	
	public static CloudService fullService() {

		// NODE OS
		Properties properties = new Properties(NodePropertiesType.OS);
		properties.addProperty("instanceType", "000000512");
		properties.addProperty("provider", "dsg@openstack");
		properties.addProperty("baseImage", "8f1428ac-f239-42e0-ab35-137f6e234101");
		properties.addProperty("packages", "java-jdk, something-something");
		properties.setId("propertiesId");

		StackNode osNode = new StackNode("osId", "Test os", 1, 2, NodeType.OS);
		osNode.addProperties(properties);

		// NODE SW 1
		ArtifactTemplate artTemplate = new ArtifactTemplate("artifactTemplateID", ArtifactType.SCRIPT);
		artTemplate.addArtifactReference(new ArtifactReference("artifactReferenceID",
				"http://128.130.172.215/salsa/upload/files/DaasService/deployCassandraSeed.sh"));
		Set<ArtifactTemplate> artifacts = new HashSet<>();
		artifacts.add(artTemplate);

		StackNode swNode = new StackNode(swNodeId, "Test node unit", 2, 5, NodeType.SOFTWARE);
		swNode.setHostNode(osNode);
		swNode.setDeploymentArtifacts(artifacts);

		ServiceUnit unit = new ServiceUnit(swNode);
		unit.addDirective(new SyblDirective("str1", DirectiveType.STRATEGY,
				"ST1: STRATEGY CASE cpuUsage < 40 % : scalein"));
		unit.addDirective(new SyblDirective("con1", DirectiveType.CONSTRAINT,
				"Co2: CONSTRAINT dataAccuracy > 95 % WHEN total_cost > 400 ;"));

		// NODE SW 2
		StackNode swNode2 = new StackNode(swNodeId2, "Test node unit 2",
				2, 5, NodeType.SOFTWARE);
		swNode2.setHostNode(osNode);

		ServiceUnit unit2 = new ServiceUnit(swNode2);

		// CONNECT TO
		ConnectToRelationship rel = new ConnectToRelationship("connectVariableName", swNode2, swNode);
		swNode2.addConnectTo(rel);

		// TOPOLOGY

		ServiceTopology topology = new ServiceTopology("topologyId");
		topology.addNode(swNode);
		topology.addNode(osNode);
		topology.addNode(swNode2);
		topology.addServiceUnit(unit);
		topology.addServiceUnit(unit2);
		topology.addDirective(new SyblDirective("con4", DirectiveType.CONSTRAINT,
				"Co4: CONSTRAINT total_cost < 800"));

		CloudService service = new CloudService(serviceId);
		service.addServiceTopology(topology);

		// INSTANCES

		NodeInstance instanceOs = new NodeInstanceOs(osNode, 0, null, State.ALLOCATING, "dsg@openstack",
				"8f1428ac-f239-42e0-ab35-137f6e234101", "000000512", "uuid_of_VM", "192.168.1.1");

		osNode.addNodeInstance(instanceOs);

		swNode.addNodeInstance(new NodeInstance(swNode, 0, State.DEPLOYED, instanceOs));
		swNode2.addNodeInstance(new NodeInstance(swNode2, 0, State.DEPLOYED, instanceOs));

		return service;

	}
}
