package at.ac.tuwien.dsg.comot.core.test;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.json.JSONObject;
import org.json.XML;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.fluent.CloudService;
import at.ac.tuwien.dsg.comot.common.test.TestUtils;
import at.ac.tuwien.dsg.comot.common.test.samples.ExampleDeployOneVM;
import at.ac.tuwien.dsg.comot.common.test.samples.ExampleExecutableOnVM;
import at.ac.tuwien.dsg.comot.cs.transformer.ToscaDescriptionBuilderImpl;
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

	@Test
	public void testExecutableOnVM() throws CoreServiceException, JAXBException, IOException {

		CloudService service = ExampleExecutableOnVM.build();

		CompositionRulesConfiguration rules = TestUtils.loadMetricCompositionRules(service.getId(),
				service.getMetricCompositonRulesFile());

		String effects = TestUtils.loadFile(service.getEffectsCompositonRulesFile());

		log.info(effects);

		orchestrator.deployAndControl(service, rules, effects);

	}

	@Test
	public void testConverting() {

		CloudService service = ExampleExecutableOnVM.build();

		String xml = new ToscaDescriptionBuilderImpl().toXml(service);
		log.info("xml: " + xml);

		JSONObject json = XML.toJSONObject(xml);

		log.info("json: " + json);

	}

}
