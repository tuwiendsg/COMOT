package at.ac.tuwien.dsg.comot.client.test;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaMappingProperties;
import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.model.ArtifactReference;
import at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;
import at.ac.tuwien.dsg.comot.common.model.elastic.Capability;
import at.ac.tuwien.dsg.comot.common.model.elastic.Requirement;
import at.ac.tuwien.dsg.comot.common.model.elastic.SyblDirective;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.cs.transformer.MyMapper;
import at.ac.tuwien.dsg.comot.cs.transformer.ToscaMapperDozer;

public class Mapping extends AbstractTest {

	protected CloudService service;

	@Autowired
	protected ToscaMapperDozer mapper;

	@Autowired
	protected MyMapper myMapper;

	@Before
	public void startup() {

		List<Capability> capabilities = new ArrayList<>();
		capabilities.add(new Capability("cap1", "variable"));
		capabilities.add(new Capability("cap2", "variable"));

		List<Requirement> requirements = new ArrayList<>();
		requirements.add(new Requirement("req1", "variable"));
		requirements.add(new Requirement("req2", "variable"));

		List<SyblDirective> directives = new ArrayList<>();
		directives.add(new SyblDirective("str1", "ST1: STRATEGY CASE cpuUsage &lt; 40 % : scalein"));
		directives.add(new SyblDirective("con1", "C2: CONSTRAINT CASE cpuUsage &lt; 40 % : scalein"));

		ServiceUnit serviceUnit = new ServiceUnit(
				"unitId", "type", "name",
				directives, requirements, capabilities, null, null,
				2, 5, null);

		ArtifactTemplate artTemplate = new ArtifactTemplate("deployCassandraNode", "sh");
		artTemplate.setName("Deployment script");
		artTemplate.addArtifactReference(new ArtifactReference("XXX should not be copied",
				"http://128.130.172.215/salsa/upload/files/DaasService/deployCassandraSeed.sh"));

		serviceUnit.addDeploymentArtifact(artTemplate);

		ServiceUnit os = new ServiceUnit(
				"osId", "type", "name", 1, 2);
		os.addProperty("instanceType", "000000512");
		os.addProperty("provider", "dsg@openstack");
		os.addProperty("baseImage", "8f1428ac-f239-42e0-ab35-137f6e234101");
		os.addProperty("packages", "java-jdk, something-something");

		os.addCapability(new Capability("cap3", "variable"));
		os.addSyblDirective(new SyblDirective("con3", "C2: CONSTRAINT CASE cpuUsage &lt; 40 % : scalein"));

		ServiceTopology topology = new ServiceTopology("topologyId");
		topology.addUnit(serviceUnit);
		topology.addUnit(os);

		service = new CloudService("serviceId");
		service.addServiceTopology(topology);
		service.addEntityRelationship(new EntityRelationship("rela1", "mytype", serviceUnit, os));

	}

	@Test
	public void dozer() throws JAXBException {

		log.info("{}", Utils.asJsonString(service));

		Definitions result = myMapper.toTosca(service);

		log.info("" + Utils.asXmlString(result, SalsaMappingProperties.class));

		CloudService resultInverse = mapper.get().map(result, CloudService.class);
		// log.info("inverse {}", Utils.asJsonString(resultInverse));

	}

}
