package at.ac.tuwien.dsg.comot.test.model.examples;

import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.comot.model.node.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.model.node.NodeInstance;
import at.ac.tuwien.dsg.comot.model.node.NodeInstanceOs;
import at.ac.tuwien.dsg.comot.model.node.Properties;
import at.ac.tuwien.dsg.comot.model.relationship.ConnectToRel;
import at.ac.tuwien.dsg.comot.model.relationship.HostOnRel;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.model.type.ArtifactType;
import at.ac.tuwien.dsg.comot.model.type.DirectiveType;
import at.ac.tuwien.dsg.comot.model.type.NodePropertiesType;
import at.ac.tuwien.dsg.comot.model.type.NodeType;
import at.ac.tuwien.dsg.comot.model.type.State;

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
		properties.setId(osNodeId + "_property_salsa");

		StackNode osNode = new StackNode(osNodeId, "Test os", 1, 2, NodeType.OS);
		osNode.addProperties(properties);

		// NODE SW 1
		StackNode swNode = new StackNode(swNodeId, "Test node unit", 2, 5, NodeType.SOFTWARE);

		// NODE SW 2
		StackNode swNode2 = new StackNode(swNodeId2, "Test node unit 2", 2, 5, NodeType.SOFTWARE);

		// HOST ON
		HostOnRel hostOn1 = new HostOnRel("hostOn1ID", swNode, osNode);
		swNode.setHostNode(hostOn1);
		HostOnRel hostOn2 = new HostOnRel("hostOn2ID", swNode2, osNode);
		swNode2.setHostNode(hostOn2);

		// CONNECT TO
		ConnectToRel rel = new ConnectToRel("connectToID", "connectVar_Capa", "connectVar_Req", swNode2, swNode);
		swNode2.addConnectTo(rel);

		// UNITS
		ServiceUnit unit = new ServiceUnit(swNode + "_unit", swNode);
		ServiceUnit unit2 = new ServiceUnit(swNode2 + "_unit", swNode2);

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

	public static CloudService fullServiceWithoutInstances() {

		// NODE OS
		Properties properties = new Properties(NodePropertiesType.OS);
		properties.addProperty("instanceType", "000000512");
		properties.addProperty("provider", "dsg@openstack");
		properties.addProperty("baseImage", "8f1428ac-f239-42e0-ab35-137f6e234101");
		properties.addProperty("packages", "java-jdk, something-something");
		properties.setId(osNodeId + "_property_salsa");

		StackNode osNode = new StackNode(osNodeId, "Test os", 1, 2, NodeType.OS);
		osNode.addProperties(properties);

		// NODE SW 1
		ArtifactTemplate artTemplate = new ArtifactTemplate("artifactTemplateID", ArtifactType.SCRIPT);
		artTemplate.addUri("http://128.130.172.215/salsa/upload/files/DaasService/deployCassandraSeed.sh");

		StackNode swNode = new StackNode(swNodeId, "Test node unit", 2, 5, NodeType.SOFTWARE);
		swNode.addDeploymentArtifact(artTemplate);

		ServiceUnit unit = new ServiceUnit(swNodeId + "_unit", swNode);
		unit.addDirective(new SyblDirective("str1", DirectiveType.STRATEGY,
				"ST1: STRATEGY CASE cpuUsage < 40 % : scalein"));
		unit.addDirective(new SyblDirective("con1", DirectiveType.CONSTRAINT,
				"Co2: CONSTRAINT dataAccuracy > 95 % WHEN total_cost > 400 ;"));

		// NODE SW 2
		StackNode swNode2 = new StackNode(swNodeId2, "Test node unit 2",
				2, 5, NodeType.SOFTWARE);

		ServiceUnit unit2 = new ServiceUnit(swNodeId2 + "_unit", swNode2);

		// HOST ON
		HostOnRel hostOn1 = new HostOnRel("hostOn1ID", swNode, osNode);
		swNode.setHostNode(hostOn1);
		HostOnRel hostOn2 = new HostOnRel("hostOn2ID", swNode2, osNode);
		swNode2.setHostNode(hostOn2);

		// CONNECT TO
		ConnectToRel rel = new ConnectToRel("connectToID", "connectVar_Capa", "connectVar_Req", swNode2, swNode);
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
		service.addArtifacts(artTemplate);
		return service;
	}

	public static CloudService fullService() {

		CloudService service = fullServiceWithoutInstances();
		NodeInstance instanceOs = null;

		// INSTANCES
		for (StackNode node : service.getServiceTopologiesList().get(0).getNodes()) {
			if (node.getId().equals(osNodeId)) {
				instanceOs = new NodeInstanceOs(osNodeId + "_instance", 0, null, State.ALLOCATING,
						"dsg@openstack",
						"8f1428ac-f239-42e0-ab35-137f6e234101", "000000512", "uuid_of_VM", "192.168.1.1");
				node.addNodeInstance(instanceOs);
			}
		}

		for (StackNode node : service.getServiceTopologiesList().get(0).getNodes()) {
			if (node.getId().equals(swNodeId)) {
				node.addNodeInstance(new NodeInstance(swNodeId + "_instance", 0, State.DEPLOYED, instanceOs));
			} else if (node.getId().equals(swNodeId2)) {
				node.addNodeInstance(new NodeInstance(swNodeId2 + "_instance", 0, State.DEPLOYED, instanceOs));
			}
		}
		return service;

	}
}
