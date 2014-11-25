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
import at.ac.tuwien.dsg.comot.cs.mapper.MelaMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.orika.MelaOrika;
import at.ac.tuwien.dsg.comot.cs.test.AbstractTest;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
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

	protected static final String TEST_SERVICE_ID = "aaaa";

	@Test
	public void mapperTest() throws JAXBException, ClassNotFoundException, IOException, CoreServiceException,
			ComotException {

		// log.info("original {}", Utils.asJsonString(serviceForMapping));

		Definitions def = salsaClient.getTosca(TEST_SERVICE_ID);
		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService serviceState;
		serviceState = salsaClient.getStatus(TEST_SERVICE_ID);

		CloudService service = mapperTosca.createModel(def);
		mapperDepl.enrichModel(service, serviceState);

		log.info("enriched {}", Utils.asJsonString(service));

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
	public void orikaTest() throws JAXBException {

		log.info("original {}", Utils.asJsonString(serviceForMapping));

		MonitoredElement element = orika.get().map(serviceForMapping, MonitoredElement.class);
		log.info("element {}", UtilsCs.asString(element));

	}

}
