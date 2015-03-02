package at.ac.tuwien.dsg.comot.m.cs.test.mapping;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.orika.ToscaOrika;
import at.ac.tuwien.dsg.comot.m.cs.test.AbstractTest;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

public class SalsaMappingTest extends AbstractTest {

	@Autowired
	protected ToscaMapper mapperTosca;
	@Autowired
	protected DeploymentMapper mapperDepl;
	@Autowired
	protected ToscaOrika toscaOrika;

	protected CloudService serviceForMapping;

	// test with https://github.com/tuwiendsg/SALSA/blob/master/examples/4-DeployWithTomcat.xml
	protected static final String TEST_SERVICE_ID = "comot_tomcat_id";

	@Before
	public void startup() {
		serviceForMapping = STemplates.fullServiceWithoutInstances();
	}

	@Test
	public void automatedMapperTest() throws JAXBException {

		log.info("original {}", Utils.asXmlString(serviceForMapping));

		Definitions tosca1 = mapperTosca.extractTosca(serviceForMapping);
		log.info("tosca1 {}", UtilsCs.asString(tosca1));

		CloudService service2 = mapperTosca.createModel(tosca1);
		log.info("service2 {}", Utils.asXmlString(service2));
		assertReflectionEquals(serviceForMapping, service2, ReflectionComparatorMode.LENIENT_ORDER);

		Definitions tosca2 = mapperTosca.extractTosca(service2);
		log.info("tosca2 {}", UtilsCs.asString(tosca2));

		CloudService service3 = mapperTosca.createModel(tosca2);
		log.info("service3 {}", Utils.asXmlString(service3));
		assertReflectionEquals(serviceForMapping, service3, ReflectionComparatorMode.LENIENT_ORDER);

	}

	@Test
	public void testToscaFromFile() throws JAXBException, IOException {

		// Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/daas_m2m_fromSalsa.xml");
		Definitions tosca1 = UtilsCs.loadTosca("./../resources/test/tomcat/tomcat_from_salsa.xml");
		log.info("tosca1 {}", UtilsCs.asString(tosca1));

		CloudService service1 = mapperTosca.createModel(tosca1);
		log.info("service1 {}", Utils.asXmlString(service1));

		Definitions tosca2 = mapperTosca.extractTosca(service1);
		log.info("tosca2 {}", UtilsCs.asString(tosca2));

		CloudService service2 = mapperTosca.createModel(tosca2);
		log.info("service2 {}", Utils.asXmlString(service2));
		assertReflectionEquals(service1, service2, ReflectionComparatorMode.LENIENT_ORDER);

	}

	@Test
	public void orikaTest() throws JAXBException {

		log.info("original {} \n", Utils.asXmlString(serviceForMapping));

		Definitions tosca1 = toscaOrika.get().map(serviceForMapping, Definitions.class);
		log.info("tosca1 {} \n", UtilsCs.asString(tosca1));

		CloudService service2 = toscaOrika.get().map(tosca1, CloudService.class);
		log.info("service2 {} \n", Utils.asXmlString(service2));

		Definitions tosca2 = toscaOrika.get().map(service2, Definitions.class);
		log.info("tosca2 {} \n", UtilsCs.asString(tosca2));

		CloudService service3 = toscaOrika.get().map(tosca2, CloudService.class);
		log.info("service3 {}", Utils.asXmlString(service3));

	}

	@Test
	public void stateMapperTest() throws CoreServiceException, JAXBException, ComotException {

		// ENRICH WITH STATE

		Definitions def = salsaClient.getTosca(TEST_SERVICE_ID);
		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService serviceState;
		serviceState = salsaClient.getStatus(TEST_SERVICE_ID);

		log.info("tosca {}", UtilsCs.asString(def));
		log.info("state {}", UtilsCs.asString(serviceState));

		CloudService service = mapperTosca.createModel(def);
		log.info("service {}", Utils.asXmlString(service));

		mapperDepl.enrichModel(service, serviceState);
		log.info("service enriched{}", Utils.asXmlString(service));

		// EXTRACT DEPLOYMENT DESCRIOPTION

		DeploymentDescription descr = mapperDepl.extractDeployment(service);
		log.info("depl {}", UtilsCs.asString(descr));

	}

}
