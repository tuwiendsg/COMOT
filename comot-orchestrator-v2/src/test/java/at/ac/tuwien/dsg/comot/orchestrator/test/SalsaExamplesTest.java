package at.ac.tuwien.dsg.comot.orchestrator.test;

import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackMicro;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.ac.tuwien.dsg.comot.client.test.TestClientContext;
import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import at.ac.tuwien.dsg.comot.core.test.TestUtils;
import at.ac.tuwien.dsg.comot.core.test.samples.ExampleDeployOneVM;
import at.ac.tuwien.dsg.comot.orchestrator.ComotOrchestrator;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

/**
 * https://github.com/tuwiendsg/SALSA/tree/master/examples
 * 
 * @author jurajcik
 *
 */
public class SalsaExamplesTest extends AbstractTest {

	private final Logger log = LoggerFactory.getLogger(SalsaExamplesTest.class);

	@Test
	public void testDeployOneVM() throws CoreServiceException, JAXBException, IOException {

		CloudService service = ExampleDeployOneVM.build();

		CompositionRulesConfiguration rules = TestUtils.loadMetricCompositionRules(service.getId(),
				service.getMetricCompositonRulesFile());

		String effects = TestUtils.loadFile(service.getEffectsCompositonRulesFile());

		log.info(effects);

		orchestrator.deployAndControl(service, rules, effects);

	}

}
