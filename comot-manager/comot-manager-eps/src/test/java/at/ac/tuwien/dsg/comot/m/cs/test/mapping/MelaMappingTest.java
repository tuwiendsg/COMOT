package at.ac.tuwien.dsg.comot.m.cs.test.mapping;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.common.model.monitoring.ElementMonitoring;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.MelaMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.MelaOutputMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.orika.MelaOrika;
import at.ac.tuwien.dsg.comot.m.cs.test.AbstractTest;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.test.model.examples.STemplates;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshot;
import at.ac.tuwien.dsg.mela.common.requirements.Requirements;

public class MelaMappingTest extends AbstractTest {

	@Autowired
	protected MelaOrika orika;
	@Autowired
	protected MelaMapper mapper;

	@Autowired
	protected ToscaMapper mapperTosca;
	@Autowired
	protected DeploymentMapper mapperDepl;
	@Autowired
	protected MelaOutputMapper mapperMelaOutput;

	protected static final String TEST_SERVICE_ID = "example_executableOnVM_1";

	protected CloudService serviceForMapping;

	@Before
	public void startup() {
		serviceForMapping = STemplates.fullService();
	}

	@Test
	public void mapperTest() throws JAXBException, ClassNotFoundException, IOException, CoreServiceException,
			ComotException {

		// log.info("original {}", Utils.asJsonString(serviceForMapping));

		Definitions def = salsaClient.getTosca(TEST_SERVICE_ID);
		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService serviceState;
		serviceState = salsaClient.getStatus(TEST_SERVICE_ID);

		log.info("enriched {}", UtilsCs.asString(serviceState));

		CloudService service = mapperTosca.createModel(def);
		mapperDepl.enrichModel(TEST_SERVICE_ID, service, serviceState);

		// log.info("enriched {}", Utils.asJsonString(service));

		MonitoredElement element = mapper.extractMela(service);
		log.info("mela {}", UtilsCs.asString(element));

	}

	@Test
	public void requirementsTest() throws JAXBException, ClassNotFoundException, IOException {

		// log.info("original {}", Utils.asJsonString(serviceForMapping));

		Requirements element = mapper.extractRequirements(serviceForMapping);
		log.info("mela {}", Utils.asXmlString(element));

	}

	@Test
	public void testMelaOutputOffline() throws JAXBException, ClassNotFoundException, IOException,
			CoreServiceException,
			ComotException {

		String melaData = Utils.loadFile(UtilsTest.TEST_FILE_BASE + "xml/ViennaChillerSensors_monitoringData.xml");

		StringReader reader = new StringReader(melaData);
		JAXBContext jaxbContext = JAXBContext.newInstance(MonitoredElementMonitoringSnapshot.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		MonitoredElementMonitoringSnapshot def = (MonitoredElementMonitoringSnapshot) jaxbUnmarshaller
				.unmarshal(reader);

		ElementMonitoring element = mapperMelaOutput.extractOutput(def);

		log.info("mela {}", Utils.asXmlString(element));
	}

}
