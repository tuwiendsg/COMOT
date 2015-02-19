package at.ac.tuwien.dsg.comot.m.core.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.oasis.tosca.Definitions;

import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.m.core.model.ServiceEntity;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public class OrchestratorTest extends AbstractTest {

	public static final String SERVICE_ID = "comot_tomcat_id";

	String newServiceId = null;

	// @Before
	// public void startUp() throws CoreServiceException {
	// log.info("START UP TEST");
	// newServiceId = null;
	//
	// for (String str : monitoring.listAllServices()) {
	// monitoring.stopMonitoring(str);
	// }
	// }
	//
	// @After
	// public void cleanUp() throws CoreServiceException {
	// log.info("CLEAN UP TEST");
	// if (newServiceId != null) {
	// deployment.undeploy(newServiceId);
	// }
	// for (String str : monitoring.listAllServices()) {
	// monitoring.stopMonitoring(str);
	// }
	// }

	// put("http://localhost:8380/comot/rest/ElasticIoTPlatform/monitoring");

	@Test
	public void dostuff() throws CoreServiceException, ComotException {
		orchestrator.getServices();
	}

	@Test
	public void testMonitoring() throws CoreServiceException, ComotException {
		// orchestrator.startMonitoring(SERVICE_ID);
	}

	@Test
	public void testRecordingManager() throws IOException, JAXBException, CoreServiceException, ComotException,
			IllegalArgumentException, IllegalAccessException {

		Definitions def = UtilsCs.loadTosca(UtilsTest.TEST_FILE_BASE + "tomcat/tomcat.xml");
		// CloudService service = mapperTosca.createModel(def);

		// deploy
		String serviceId = orchestrator.deployNew(UtilsCs.asString(def));

		ServiceEntity entity = serviceRepo.findOne(serviceId);

		log.info("entity: {}", entity);

		UtilsTest.sleepInfinit();

	}

	@Test
	public void testDeploymentWithMonitoring() throws CoreServiceException, ComotException, JAXBException, IOException {

		newServiceId = SERVICE_ID + "_test1";

		CloudService service = orchestrator.getService(SERVICE_ID);
		service.setId(newServiceId);

		CompositionRulesConfiguration mcr = UtilsCs.loadMetricCompositionRules("test_mcr",
				"./../resources/test/mela/compositionRules.xml");

		log.info(" {} ", mcr);

		// deploy
		orchestrator.deployNew(UtilsCs.asString(mapperTosca.extractTosca(service)));
		orchestrator.setMcr(newServiceId, mcr);
		// orchestrator.startMonitoring(newServiceId);

		UtilsTest.sleepInfinit();

		// UtilsTest.sleepSeconds(10);
		// orchestrator.startMonitoring("ElasticIoTPlatform");

	}
}