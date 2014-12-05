package at.ac.tuwien.dsg.comot.graph.test;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import at.ac.tuwien.dsg.comot.graph.model.EntityRelationship;
import at.ac.tuwien.dsg.comot.graph.model.SyblDirective;
import at.ac.tuwien.dsg.comot.graph.model.node.ArtifactReference;
import at.ac.tuwien.dsg.comot.graph.model.node.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.graph.model.node.Capability;
import at.ac.tuwien.dsg.comot.graph.model.node.Properties;
import at.ac.tuwien.dsg.comot.graph.model.node.Requirement;
import at.ac.tuwien.dsg.comot.graph.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.graph.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.graph.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.graph.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.graph.model.type.ArtifactType;
import at.ac.tuwien.dsg.comot.graph.model.type.CapabilityType;
import at.ac.tuwien.dsg.comot.graph.model.type.DirectiveType;
import at.ac.tuwien.dsg.comot.graph.model.type.NodePropertiesType;
import at.ac.tuwien.dsg.comot.graph.model.type.NodeType;
import at.ac.tuwien.dsg.comot.graph.model.type.RelationshipType;
import at.ac.tuwien.dsg.comot.graph.model.type.RequirementType;
import at.ac.tuwien.dsg.comot.graph.model.type.State;
import at.ac.tuwien.dsg.comot.graph.model.unit.NodeInstance;
import at.ac.tuwien.dsg.comot.graph.model.unit.NodeInstanceOs;

public class ConvertingToGraph extends AbstractTest {

	protected CloudService service;
	protected String swNodeId = "nodeId";
	protected String serviceId = "serviceId";

	@Before
	public void startup() {

		Set<Capability> capabilities = new HashSet<>();
		capabilities.add(new Capability("cap1", CapabilityType.VARIABLE));
		capabilities.add(new Capability("cap2", CapabilityType.VARIABLE));

		Set<Requirement> requirements = new HashSet<>();
		requirements.add(new Requirement("req1", RequirementType.VARIABLE));
		Requirement req2 = new Requirement("req2", RequirementType.VARIABLE);
		requirements.add(req2);

		Set<SyblDirective> directives = new HashSet<>();
		directives.add(new SyblDirective("str1", DirectiveType.STRATEGY,
				"ST1: STRATEGY CASE cpuUsage < 40 % : scalein"));
		directives
				.add(new SyblDirective(
						"con1",
						DirectiveType.CONSTRAINT,
						"Co2: CONSTRAINT dataAccuracy > 95 % WHEN total_cost > 400 ;"));

		StackNode swNode = new StackNode(swNodeId, "Test node unit",
				2, 5, NodeType.SOFTWARE, requirements, capabilities, null, null);

		ServiceUnit unit = new ServiceUnit(swNode, directives);

		ArtifactTemplate artTemplate = new ArtifactTemplate("deployCassandraNode", ArtifactType.SCRIPT);
		artTemplate.setName("Deployment script");
		artTemplate.addArtifactReference(new ArtifactReference("XXX should not be copied",
				"http://128.130.172.215/salsa/upload/files/DaasService/deployCassandraSeed.sh"));

		swNode.addDeploymentArtifact(artTemplate);

		Properties properties = new Properties(NodePropertiesType.OS);
		properties.addProperty("instanceType", "000000512");
		properties.addProperty("provider", "dsg@openstack");
		properties.addProperty("baseImage", "8f1428ac-f239-42e0-ab35-137f6e234101");
		properties.addProperty("packages", "java-jdk, something-something");

		StackNode osNode = new StackNode("osId", "Test os", 1, 2, NodeType.OS);

		Capability cap3 = new Capability("cap3", CapabilityType.VARIABLE);
		osNode.addProperties(properties);
		osNode.addCapability(cap3);

		ServiceTopology topology = new ServiceTopology("topologyId");
		topology.addNode(swNode);
		topology.addNode(osNode);
		topology.addServiceUnit(unit);
		topology.addSyblDirective(new SyblDirective("con4", DirectiveType.CONSTRAINT,
				"Co4: CONSTRAINT total_cost < 800"));

		service = new CloudService(serviceId);
		service.addServiceTopology(topology);

//		// old way to capture relationships
//		service.addEntityRelationship(new EntityRelationship("rela1", RelationshipType.HOST_ON,
//				swNode.getId(), osNode.getId()));
//		service
//				.addEntityRelationship(new EntityRelationship("rela2", RelationshipType.CONNECT_TO,
//						req2.getId(), cap3.getId()));

		/*
		 * added for neo4j testing
		 */

		swNode.addNodeInstance(new NodeInstance(0, 0, State.DEPLOYED));
		osNode.addNodeInstance(new NodeInstanceOs(0, 0, State.ALLOCATING, "dsg@openstack",
				"8f1428ac-f239-42e0-ab35-137f6e234101", "000000512", "uuid_of_VM", "192.168.1.1"));

		
		swNode.setHostsOn(osNode);  // sample relationship 
	}

	// start and check at http://127.0.0.1:7474/
	@Test
	public void testStuff() throws InterruptedException {

		serviceRepo.save(service);

		while (true) {
			Thread.sleep(1000);
		}
	}

}
