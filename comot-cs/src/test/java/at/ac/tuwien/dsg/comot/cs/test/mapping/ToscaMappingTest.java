package at.ac.tuwien.dsg.comot.cs.test.mapping;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.orika.ToscaOrika;
import at.ac.tuwien.dsg.comot.cs.test.AbstractTest;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

public class ToscaMappingTest extends AbstractTest {

	@Autowired
	protected ToscaMapper mapperTosca;
	@Autowired
	protected DeploymentMapper mapperDepl;

	// test with https://github.com/tuwiendsg/SALSA/blob/master/examples/4-DeployWithTomcat.xml
	protected static final String TEST_SERVICE_ID = "aaaa";

	@Test
	public void mapperTest() throws JAXBException {

		log.info("original {}", Utils.asJsonString(serviceForMapping));

		Definitions tosca1 = mapperTosca.extractTosca(serviceForMapping);
		log.info("tosca1 {}", UtilsCs.asString(tosca1));

		CloudService service2 = mapperTosca.createModel(tosca1);
		log.info("service2 {}", Utils.asJsonString(service2));

		Definitions tosca2 = mapperTosca.extractTosca(service2);
		log.info("tosca2 {}", UtilsCs.asString(tosca2));

		CloudService service3 = mapperTosca.createModel(tosca1);
		log.info("service3 {}", Utils.asJsonString(service3));

	}

	@Test
	public void orikaTest() throws JAXBException {

		ToscaOrika toscaOrika = new ToscaOrika();

		log.info("original {}", Utils.asJsonString(serviceForMapping));

		Definitions tosca1 = toscaOrika.get().map(serviceForMapping, Definitions.class);
		log.info("tosca1 {}", UtilsCs.asString(tosca1));

		CloudService service2 = toscaOrika.get().map(tosca1, CloudService.class);
		log.info("service2 {}", Utils.asJsonString(service2));

		Definitions tosca2 = toscaOrika.get().map(service2, Definitions.class);
		log.info("tosca2 {}", UtilsCs.asString(tosca2));

		CloudService service3 = toscaOrika.get().map(tosca2, CloudService.class);
		log.info("service3 {}", Utils.asJsonString(service3));

	}

	@Test
	public void stateMapperTest() throws CoreServiceException, JAXBException, ComotException {

		Definitions def = salsaClient.getTosca(TEST_SERVICE_ID);
		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService serviceState;
		serviceState = salsaClient.getStatus(TEST_SERVICE_ID);

		log.info("tosca {}", UtilsCs.asString(def));
		log.info("state {}", UtilsCs.asString(serviceState));

		CloudService service = mapperTosca.createModel(def);
		// log.info("service {}", Utils.asJsonString(service));

		mapperDepl.enrichModel(service, serviceState);
		log.info("service enriched{}", Utils.asJsonString(service));

	}

	@Test
	public void deploymentTest() throws JAXBException, ClassNotFoundException, IOException, CoreServiceException,
			ComotException {

		Definitions def = salsaClient.getTosca(TEST_SERVICE_ID);
		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService serviceState;
		serviceState = salsaClient.getStatus(TEST_SERVICE_ID);

		CloudService service = mapperTosca.createModel(def);
		mapperDepl.enrichModel(service, serviceState);

		DeploymentDescription descr = mapperDepl.extractDeployment(service);
		log.info("depl {}", UtilsCs.asString(descr));

	}

}
