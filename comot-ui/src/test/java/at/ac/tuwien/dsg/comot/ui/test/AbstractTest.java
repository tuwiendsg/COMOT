package at.ac.tuwien.dsg.comot.ui.test;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.ac.tuwien.dsg.comot.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;
import at.ac.tuwien.dsg.comot.common.model.SyblDirective;
import at.ac.tuwien.dsg.comot.common.model.logic.Navigator;
import at.ac.tuwien.dsg.comot.common.model.node.ArtifactReference;
import at.ac.tuwien.dsg.comot.common.model.node.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.node.Capability;
import at.ac.tuwien.dsg.comot.common.model.node.Properties;
import at.ac.tuwien.dsg.comot.common.model.node.Requirement;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.common.model.type.ArtifactType;
import at.ac.tuwien.dsg.comot.common.model.type.CapabilityType;
import at.ac.tuwien.dsg.comot.common.model.type.DirectiveType;
import at.ac.tuwien.dsg.comot.common.model.type.NodePropertiesType;
import at.ac.tuwien.dsg.comot.common.model.type.NodeType;
import at.ac.tuwien.dsg.comot.common.model.type.RelationshipType;
import at.ac.tuwien.dsg.comot.common.model.type.RequirementType;
import at.ac.tuwien.dsg.comot.core.ComotOrchestrator;
import at.ac.tuwien.dsg.comot.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.core.spring.AppContextInsertData;
import at.ac.tuwien.dsg.comot.cs.connector.SalsaClient;
import at.ac.tuwien.dsg.comot.ui.service.ServicesResource;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.AssociatedVM;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppContextCore.class })
@ActiveProfiles({ AppContextCore.SPRING_PROFILE_TEST, AppContextCore.SPRING_PROFILE_INSERT_DATA })
// @TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
// @DatabaseSetup("classpath:iata_codes/airports_functional.xml")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractTest {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	protected Environment env;

	@Autowired
	protected SalsaClient salsaClient;

	@Autowired
	protected DeploymentClient deployment;
	@Autowired
	protected ControlClient control;
	@Autowired
	protected MonitoringClient monitoring;
	
	@Autowired
	protected ComotOrchestrator orchestrator;
	@Autowired
	protected ServicesResource servicesResource;
	

	protected CloudService serviceForMapping;
	protected String swNodeId = "nodeId";
	protected String serviceId = "serviceId";

	protected DeploymentDescription deploymentDescription;

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

		serviceForMapping = new CloudService(serviceId);
		serviceForMapping.addServiceTopology(topology);

		Navigator navigator = new Navigator(serviceForMapping);

		serviceForMapping.addEntityRelationship(new EntityRelationship("rela1", RelationshipType.HOST_ON,
				swNode, osNode));
		serviceForMapping
				.addEntityRelationship(new EntityRelationship("rela2", RelationshipType.CONNECT_TO,
						req2, cap3));

		// Deployment description

		AssociatedVM vm = new AssociatedVM();
		vm.setIp("10.99.0.85");
		vm.setUuid("93d785cc-f915-4127-81eb-0797b75de1a6");

		List<AssociatedVM> list = new ArrayList<>();
		list.add(vm);

		DeploymentUnit dUnit = new DeploymentUnit();
		dUnit.setServiceUnitID(swNodeId);
		dUnit.setAssociatedVM(list);

		List<DeploymentUnit> deployments = new ArrayList<>();
		deployments.add(dUnit);

		deploymentDescription = new DeploymentDescription();
		deploymentDescription.setAccessIP("localhost");
		deploymentDescription.setCloudServiceID(serviceId);
		deploymentDescription.setDeployments(deployments);
	}

}
