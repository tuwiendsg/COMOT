package at.ac.tuwien.dsg.comot.test.model.examples;

import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.comot.model.node.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.model.node.Properties;
import at.ac.tuwien.dsg.comot.model.node.UnitInstance;
import at.ac.tuwien.dsg.comot.model.node.UnitInstanceOs;
import at.ac.tuwien.dsg.comot.model.relationship.ConnectToRel;
import at.ac.tuwien.dsg.comot.model.relationship.HostOnRel;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.type.ArtifactType;
import at.ac.tuwien.dsg.comot.model.type.DirectiveType;
import at.ac.tuwien.dsg.comot.model.type.NodePropertiesType;
import at.ac.tuwien.dsg.comot.model.type.NodeType;
import at.ac.tuwien.dsg.comot.model.type.State;

public class STemplates {

	public static final String swNodeId = "nodeId";
	public static final String swNodeId2 = "nodeId2";
	public static final String osNodeId = "osId";
	public static final String topologyId = "topologyId";
	public static final String serviceId = "serviceId";

	public static CloudService simplifiedService() {

		// UNIT OS
		Properties properties = new Properties(NodePropertiesType.OS);
		properties.addProperty("instanceType", "000000512");
		properties.addProperty("provider", "dsg@openstack");
		properties.setId(osNodeId + "_property_salsa");

		ServiceUnit unitOs = new ServiceUnit(osNodeId, "Test os", 1, 2, NodeType.OS);
		unitOs.addProperties(properties);

		// UNIT SW 1
		ServiceUnit unitSw = new ServiceUnit(swNodeId, "Test node unit", 2, 5, NodeType.SOFTWARE);

		// UNIT SW 2
		ServiceUnit unitSw2 = new ServiceUnit(swNodeId2, "Test node unit 2", 2, 5, NodeType.SOFTWARE);

		// HOST ON
		HostOnRel hostOn1 = new HostOnRel("hostOn1ID", unitSw, unitOs);
		unitSw.setHost(hostOn1);
		HostOnRel hostOn2 = new HostOnRel("hostOn2ID", unitSw2, unitOs);
		unitSw2.setHost(hostOn2);

		// CONNECT TO
		ConnectToRel rel = new ConnectToRel("connectToID", "connectVar_Capa", "connectVar_Req", unitSw2, unitSw);
		unitSw2.addConnectTo(rel);

		// TOPOLOGY
		ServiceTopology topology = new ServiceTopology(topologyId);
		topology.addServiceUnit(unitSw);
		topology.addServiceUnit(unitSw2);
		topology.addServiceUnit(unitOs);

		CloudService service = new CloudService(serviceId);
		service.addServiceTopology(topology);
		return service;
	}

	public static CloudService fullServiceWithoutInstances() {

		// UNIT OS
		Properties properties = new Properties(NodePropertiesType.OS);
		properties.addProperty("instanceType", "000000512");
		properties.addProperty("provider", "dsg@openstack");
		properties.addProperty("baseImage", "8f1428ac-f239-42e0-ab35-137f6e234101");
		properties.addProperty("packages", "java-jdk, something-something");
		properties.setId(osNodeId + "_property_salsa");

		ServiceUnit unitOs = new ServiceUnit(osNodeId, "Test os", 1, 2, NodeType.OS);
		unitOs.addProperties(properties);

		// UNIT SW 1
		ArtifactTemplate artTemplate = new ArtifactTemplate("artifactTemplateID", ArtifactType.SCRIPT);
		artTemplate.addUri("http://128.130.172.215/salsa/upload/files/DaasService/deployCassandraSeed.sh");

		ServiceUnit unitSw = new ServiceUnit(swNodeId, "Test node unit", 2, 5, NodeType.SOFTWARE);
		unitSw.addDeploymentArtifact(artTemplate);

		unitSw.addDirective(new SyblDirective("str1", DirectiveType.STRATEGY,
				"ST1: STRATEGY CASE cpuUsage < 40 % : scalein"));
		unitSw.addDirective(new SyblDirective("con1", DirectiveType.CONSTRAINT,
				"Co2: CONSTRAINT dataAccuracy > 95 % WHEN total_cost > 400 ;"));

		// UNIT SW 2
		ServiceUnit unitSw2 = new ServiceUnit(swNodeId2, "Test node unit 2",
				2, 5, NodeType.SOFTWARE);

		// HOST ON
		HostOnRel hostOn1 = new HostOnRel("hostOn1ID", unitSw, unitOs);
		unitSw.setHost(hostOn1);
		HostOnRel hostOn2 = new HostOnRel("hostOn2ID", unitSw2, unitOs);
		unitSw2.setHost(hostOn2);

		// CONNECT TO
		ConnectToRel rel = new ConnectToRel("connectToID", "connectVar_Capa", "connectVar_Req", unitSw2, unitSw);
		unitSw2.addConnectTo(rel);

		// TOPOLOGY
		ServiceTopology topology = new ServiceTopology("topologyId");
		topology.addServiceUnit(unitSw);
		topology.addServiceUnit(unitOs);
		topology.addServiceUnit(unitSw2);
		topology.addDirective(new SyblDirective("con4", DirectiveType.CONSTRAINT,
				"Co4: CONSTRAINT total_cost < 800"));

		CloudService service = new CloudService(serviceId);
		service.addServiceTopology(topology);
		service.addArtifacts(artTemplate);
		return service;
	}

	public static CloudService fullService() {

		CloudService service = fullServiceWithoutInstances();
		UnitInstance instanceOs = null;

		// INSTANCES
		for (ServiceUnit unit : service.getServiceTopologiesList().get(0).getServiceUnits()) {
			if (unit.getId().equals(osNodeId)) {
				instanceOs = new UnitInstanceOs(osNodeId + "_instance", 0, null, State.ALLOCATING,
						"dsg@openstack",
						"8f1428ac-f239-42e0-ab35-137f6e234101", "000000512", "uuid_of_VM", "192.168.1.1");
				unit.addNodeInstance(instanceOs);
			}
		}

		for (ServiceUnit unit : service.getServiceTopologiesList().get(0).getServiceUnits()) {
			if (unit.getId().equals(swNodeId)) {
				unit.addNodeInstance(new UnitInstance(swNodeId + "_instance", 0, State.DEPLOYED, instanceOs));
			} else if (unit.getId().equals(swNodeId2)) {
				unit.addNodeInstance(new UnitInstance(swNodeId2 + "_instance", 0, State.DEPLOYED, instanceOs));
			}
		}
		return service;

	}
}
