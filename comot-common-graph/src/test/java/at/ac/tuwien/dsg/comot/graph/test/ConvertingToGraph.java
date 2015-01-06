package at.ac.tuwien.dsg.comot.graph.test;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import at.ac.tuwien.dsg.comot.graph.model.ConnectToRelationship;
import at.ac.tuwien.dsg.comot.graph.model.ContractItem;
import at.ac.tuwien.dsg.comot.graph.model.SyblDirective;
import at.ac.tuwien.dsg.comot.graph.model.XSelectable;
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

public class ConvertingToGraph extends AbstractTest {

	protected CloudService service;
	protected static String swNodeId = "nodeId";
	protected static String swNodeId2 = "nodeId2";
	protected static String serviceId = "serviceId";

	@Before
	public void startup() {
		service = createService();
	}

	public static CloudService createService() {

		// NODE OS
		Properties properties = new Properties(NodePropertiesType.OS);
		properties.addProperty("instanceType", "000000512");
		properties.addProperty("provider", "dsg@openstack");
		properties.addProperty("baseImage", "8f1428ac-f239-42e0-ab35-137f6e234101");
		properties.addProperty("packages", "java-jdk, something-something");

		StackNode osNode = new StackNode("osId", "Test os", 1, 2, NodeType.OS);
		osNode.addProperties(properties);

		// NODE SW 1

		Set<SyblDirective> directives = new HashSet<>();
		directives
				.add(new SyblDirective("str1", DirectiveType.STRATEGY, "ST1: STRATEGY CASE cpuUsage < 40 % : scalein"));
		directives
				.add(new SyblDirective(
						"con1",
						DirectiveType.CONSTRAINT,
						"Co2: CONSTRAINT dataAccuracy > 95 % WHEN total_cost > 400 ;"));

		ArtifactTemplate artTemplate = new ArtifactTemplate("artifactTemplateID", ArtifactType.SCRIPT);
		artTemplate.addArtifactReference(new ArtifactReference("artifactReferenceID",
				"http://128.130.172.215/salsa/upload/files/DaasService/deployCassandraSeed.sh"));
		Set<ArtifactTemplate> artifacts = new HashSet<>();
		artifacts.add(artTemplate);

		StackNode swNode = new StackNode(swNodeId, "Test node unit", 2, 5, NodeType.SOFTWARE, null, artifacts);
		swNode.setHostNode(osNode);

		ServiceUnit unit = new ServiceUnit(swNode, directives);

		// NODE SW 2
		StackNode swNode2 = new StackNode(swNodeId2, "Test node unit 2",
				2, 5, NodeType.SOFTWARE, null, null);
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
		topology.addSyblDirective(new SyblDirective("con4", DirectiveType.CONSTRAINT,
				"Co4: CONSTRAINT total_cost < 800"));

		CloudService service = new CloudService(serviceId);
		service.addServiceTopology(topology);

		// INSTANCES

		NodeInstance instanceOs = new NodeInstanceOs(0, null, State.ALLOCATING, "dsg@openstack",
				"8f1428ac-f239-42e0-ab35-137f6e234101", "000000512", "uuid_of_VM", "192.168.1.1");

		osNode.addNodeInstance(instanceOs);

		swNode.addNodeInstance(new NodeInstance(0, State.DEPLOYED, instanceOs));
		swNode2.addNodeInstance(new NodeInstance(0, State.DEPLOYED, instanceOs));

		return service;

	}

	// start and check at http://127.0.0.1:7474/
	@Test
	public void testStuff() throws InterruptedException {

		CloudService serviceFromDb = serviceRepo.save(service);

		ContractItem item = new ContractItem();
		item.addProperty(serviceFromDb);
		item.addProperty((XSelectable) serviceFromDb.getServiceTopologies().toArray()[0]);

		contractItemRepo.save(item);

		while (true) {
			Thread.sleep(1000);
		}
	}

}
