package at.ac.tuwien.dsg.comot.orchestrator.test;

import static at.ac.tuwien.dsg.comot.common.model.CloudService.ServiceTemplate;
import static at.ac.tuwien.dsg.comot.common.model.CommonOperatingSystemSpecification.OpenstackMicro;
import static at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit.OperatingSystemUnit;
import static at.ac.tuwien.dsg.comot.common.model.ServiceTopology.ServiceTopology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.common.model.OperatingSystemUnit;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import at.ac.tuwien.dsg.comot.orchestrator.ComotOrchestrator;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

/**
 * https://github.com/tuwiendsg/SALSA/tree/master/examples
 * 
 * @author jurajcik
 *
 */
public class SalsaExamplesTest {

	private final Logger log = LoggerFactory.getLogger(SalsaExamplesTest.class);

	private static final String SALSA_IP = "128.130.172.215";
	private static final int SALSA_PORT = 8080;

	private static final String serviceId = "example_deployOneVM";
	private static final String topologyId = "example_topology";
	private static final String nodeId = "example_OS";

	@Test
	public void testDeployOneVM() throws CoreServiceException {

		OperatingSystemUnit dataControllerVM = OperatingSystemUnit(nodeId)
				.providedBy(OpenstackMicro("example_VM")
						.addSoftwarePackage("openjdk-7-jre")
				);

		ServiceTopology topology = ServiceTopology(topologyId)
				.withServiceUnits(dataControllerVM);

		CloudService serviceTemplate = ServiceTemplate(serviceId)
				.consistsOfTopologies(topology)
				.withDefaultMetrics()
				.withDefaultActionEffects();

		ComotOrchestrator orchestrator = new ComotOrchestrator()
				.withSalsaIP(SALSA_IP)
				.withSalsaPort(SALSA_PORT)
				.withRsyblIP("localhost")
				.withRsyblPort(8280);

		CompositionRulesConfiguration rules = loadMetricCompositionRules(serviceId,
				serviceTemplate.getMetricCompositonRulesFile());
		
		String effects = loadJSONEffects(serviceTemplate.getEffectsCompositonRulesFile());
		
		orchestrator.deployAndControl(serviceTemplate, rules, effects);

	}

	@Test
	public void testTemp() {

		File file = new File("./config/resources/compositionRules.xml");

		log.info("" + file);
	}
	


	protected CompositionRulesConfiguration loadMetricCompositionRules(String serviceID, String path) {
		CompositionRulesConfiguration compositionRulesConfiguration = null;
		try {
			JAXBContext a = JAXBContext.newInstance(CompositionRulesConfiguration.class);
			Unmarshaller u = a.createUnmarshaller();

			Object object = u.unmarshal(new FileReader(new File(path)));
			compositionRulesConfiguration = (CompositionRulesConfiguration) object;

		} catch (JAXBException e) {
			log.error(e.getStackTrace().toString());
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		compositionRulesConfiguration.setTargetServiceID(serviceID);

		return compositionRulesConfiguration;
	}

	protected String loadJSONEffects(String path) {
		String json = "";
		String line = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			while ((line = reader.readLine()) != null) {
				json += line;
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return json;
	}

}
