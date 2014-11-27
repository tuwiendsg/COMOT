package at.ac.tuwien.dsg.comot.core.test;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.test.UtilsTest;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public class OrchestratorTest extends AbstractTest {

	public static final String SERVICE_ID = "comot_tomcat_id";

	String newServiceId = null;

	@Before
	public void startUp() throws CoreServiceException {
		newServiceId = null;

		for (String str : monitoring.listAllServices()) {
			monitoring.stopMonitoring(str);
		}
	}

	@After
	public void cleanUp() throws CoreServiceException {
		if (newServiceId != null) {
			deployment.undeploy(newServiceId);
		}
		for (String str : monitoring.listAllServices()) {
			monitoring.stopMonitoring(str);
		}
	}

	// put("http://localhost:8380/comot/rest/ElasticIoTPlatform/monitoring");

	@Test
	public void dostuff() throws CoreServiceException, ComotException {
		
	}
	
	@Test
	public void testMonitoring() throws CoreServiceException, ComotException {
		//orchestrator.startMonitoring(SERVICE_ID);
	}

	@Test
	public void testDeploymentWithMonitoring() throws CoreServiceException, ComotException, FileNotFoundException,
			JAXBException {

		newServiceId = SERVICE_ID + "_test1";

		CloudService service = orchestrator.getService(SERVICE_ID);
		service.setId(newServiceId);

		CompositionRulesConfiguration mcr = UtilsTest.loadMetricCompositionRules("test_mcr",
				"./elasticity/compositionRules.xml");

		// deploy
		orchestrator.deployNew(service);
		orchestrator.setMcr(newServiceId, mcr);
		orchestrator.startMonitoring(newServiceId);

		UtilsTest.sleepInfinit();

		// UtilsTest.sleepSeconds(10);
		// orchestrator.startMonitoring("ElasticIoTPlatform");

	}
}
