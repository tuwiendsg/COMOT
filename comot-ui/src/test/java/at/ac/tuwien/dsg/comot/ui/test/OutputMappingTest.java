package at.ac.tuwien.dsg.comot.ui.test;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;
import org.oasis.tosca.Definitions;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaInstanceDescription_VM;
import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaMappingProperties;
import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.ui.mapper.SalsaOutputMapper;
import at.ac.tuwien.dsg.comot.ui.model.ElementState;

public class OutputMappingTest extends AbstractTest {

	@Autowired
	protected ToscaMapper mapperTosca;
	@Autowired
	protected DeploymentMapper mapperDepl;

	@Autowired
	protected SalsaOutputMapper mapperOutput;

	// test with https://github.com/tuwiendsg/SALSA/blob/master/examples/4-DeployWithTomcat.xml
	protected static final String TEST_SERVICE_ID = "ViennaChillerSensors";

	@Test
	public void testSalsaOutputOffline() throws JAXBException, ClassNotFoundException, IOException,
			CoreServiceException,
			ComotException {

		String salsaTosca = UtilsTest.loadFile("./tomcat/tomcat_from_salsa.xml");
		String salsaStatus = UtilsTest.loadFile("./tomcat/tomcat_status_multi.xml");

		StringReader reader = new StringReader(salsaTosca);
		JAXBContext jaxbContext = JAXBContext.newInstance(Definitions.class, SalsaMappingProperties.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		Definitions def = (Definitions) jaxbUnmarshaller.unmarshal(reader);

		reader = new StringReader(salsaStatus);
		jaxbContext = JAXBContext.newInstance(
				at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService.class,
				SalsaInstanceDescription_VM.class);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService serviceState;
		serviceState = (at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService) jaxbUnmarshaller
				.unmarshal(reader);

		CloudService service = mapperTosca.createModel(def);
		mapperDepl.enrichModel(service, serviceState);

		ElementState element = mapperOutput.extractOutput(service);
		log.info("depl {}", Utils.asXmlString(element));

	}

	@Test
	public void testSalsaOutput() throws JAXBException, ClassNotFoundException, IOException, CoreServiceException,
			ComotException {

		Definitions def = salsaClient.getTosca(TEST_SERVICE_ID);
		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService serviceState;
		serviceState = salsaClient.getStatus(TEST_SERVICE_ID);
		log.info("state {}", UtilsCs.asString(serviceState));

		CloudService service = mapperTosca.createModel(def);
		mapperDepl.enrichModel(service, serviceState);

		ElementState element = mapperOutput.extractOutput(service);
		log.info("depl {}", Utils.asXmlString(element));

	}

}
