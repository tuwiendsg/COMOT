package at.ac.tuwien.dsg.comot.cs.test;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.ac.tuwien.dsg.comot.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;
import at.ac.tuwien.dsg.comot.common.model.Navigator;
import at.ac.tuwien.dsg.comot.common.model.SyblDirective;
import at.ac.tuwien.dsg.comot.common.model.node.ArtifactReference;
import at.ac.tuwien.dsg.comot.common.model.node.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.node.Capability;
import at.ac.tuwien.dsg.comot.common.model.node.Properties;
import at.ac.tuwien.dsg.comot.common.model.node.Requirement;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.type.ArtifactType;
import at.ac.tuwien.dsg.comot.common.model.type.CapabilityType;
import at.ac.tuwien.dsg.comot.common.model.type.DirectiveType;
import at.ac.tuwien.dsg.comot.common.model.type.RelationshipType;
import at.ac.tuwien.dsg.comot.common.model.type.RequirementType;
import at.ac.tuwien.dsg.comot.common.model.type.NodePropertiesType;
import at.ac.tuwien.dsg.comot.common.model.type.NodeType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestCSContext.class })
// @ActiveProfiles({ ApplicationContext.SPRING_PROFILE_TEST })
// @TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
// @DatabaseSetup("classpath:iata_codes/airports_functional.xml")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractTest {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	protected Environment env;

	@Autowired
	protected DeploymentClient deployment;
	@Autowired
	protected ControlClient control;
	@Autowired
	protected MonitoringClient monitoring;

	protected CloudService serviceForMapping;
	protected String swNodeId = "nodeId";
	protected String serviceId = "serviceId";

	@Before
	public void startup() {

		List<Capability> capabilities = new ArrayList<>();
		capabilities.add(new Capability("cap1", CapabilityType.VARIABLE));
		capabilities.add(new Capability("cap2", CapabilityType.VARIABLE));

		List<Requirement> requirements = new ArrayList<>();
		requirements.add(new Requirement("req1", RequirementType.VARIABLE));
		Requirement req2 = new Requirement("req2", RequirementType.VARIABLE);
		requirements.add(req2);

		List<SyblDirective> directives = new ArrayList<>();
		directives.add(new SyblDirective("str1", DirectiveType.STRATEGY,
				"ST1: STRATEGY CASE cpuUsage &lt; 40 % : scalein"));
		directives.add(new SyblDirective("con1", DirectiveType.CONSTRAINT,
				"C2: CONSTRAINT CASE cpuUsage &lt; 40 % : scalein"));

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
		osNode.setProperties(properties);
		osNode.addCapability(cap3);

		ServiceTopology topology = new ServiceTopology("topologyId");
		topology.addNode(swNode);
		topology.addNode(osNode);
		topology.addServiceUnit(unit);
		topology.addSyblDirective(new SyblDirective("con4", DirectiveType.CONSTRAINT,
				"C2: CONSTRAINT CASE cpuUsage &lt; 40 % : scalein"));

		serviceForMapping = new CloudService(serviceId);
		serviceForMapping.addServiceTopology(topology);

		Navigator navigator = new Navigator(serviceForMapping);

		serviceForMapping.addEntityRelationship(new EntityRelationship("rela1", RelationshipType.HOST_ON,
				swNode, osNode,
				navigator.resolveToServicePart(swNode),
				navigator.resolveToServicePart(osNode)));
		serviceForMapping
				.addEntityRelationship(new EntityRelationship("rela2", RelationshipType.CONNECT_TO,
						req2, cap3,
						navigator.resolveToServicePart(req2),
						navigator.resolveToServicePart(cap3)));
	}

}
