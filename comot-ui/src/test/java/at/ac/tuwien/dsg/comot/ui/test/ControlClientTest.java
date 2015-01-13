package at.ac.tuwien.dsg.comot.ui.test;

import generated.oasis.tosca.TDefinitions;
import generated.oasis.tosca.TNodeTemplate;
import generated.oasis.tosca.TServiceTemplate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.junit.Test;

import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.test.UtilsTest;
import at.ac.tuwien.dsg.comot.test.model.examples.ExampleDeployOneVM;
import at.ac.tuwien.dsg.comot.ui.model.UnifiedConfiguration;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public class ControlClientTest extends AbstractTest {

	@Test
	public void testTest() throws CoreServiceException, ComotException {

		deployment.deploy(ExampleDeployOneVM.build());
	}

	@Test
	public void testJaxb() throws CoreServiceException, JAXBException, IOException {

		CompositionRulesConfiguration mcr = UtilsTest.loadMetricCompositionRules("aaaa",
				"./mela/defCompositionRules.xml");

		UnifiedConfiguration unified = new UnifiedConfiguration();
		unified.setEffects("effects");
		unified.setMcr(mcr);

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(UnifiedConfiguration.class);

			// marshall
			StringWriter sw = new StringWriter();

			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(JAXBContextProperties.MEDIA_TYPE, "application/json");
			marshaller.setProperty(JAXBContextProperties.JSON_INCLUDE_ROOT, true);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			marshaller.marshal(unified, sw);

			String xml = sw.toString();
			log.info("marshalled:" + xml);

			// unmarshall
			StringReader reader = new StringReader(xml);

			Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
			unMarshaller.setProperty(JAXBContextProperties.MEDIA_TYPE, "application/json");
			unMarshaller.setProperty(JAXBContextProperties.JSON_INCLUDE_ROOT, true);

			UnifiedConfiguration customer = (UnifiedConfiguration) unMarshaller.unmarshal(reader);

			log.info("" + customer);

			sw = new StringWriter();
			marshaller.marshal(customer.getMcr(), sw);
			log.info(sw.toString());

		} catch (JAXBException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testTosca() throws FileNotFoundException, JAXBException {

		TDefinitions tosca = loadTosca("./tomcat/tomcat.xml");

		TServiceTemplate serviceTemplate = (TServiceTemplate) tosca
				.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().get(0);
		TNodeTemplate nodeTemplate = (TNodeTemplate) serviceTemplate.getTopologyTemplate()
				.getNodeTemplateOrRelationshipTemplate().get(0);

		log.info("{}", Utils.asXmlString(tosca));

		// CloudService service = SalsaToscaDeployer.buildRuntimeDataFromTosca(tosca);

		// log.info("{}", Utils.asXmlString(service));
	}

	public static TDefinitions loadTosca(String path)
			throws JAXBException, FileNotFoundException {

		TDefinitions xmlContent = null;

		JAXBContext context = JAXBContext.newInstance(TDefinitions.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();

		xmlContent = (TDefinitions) unmarshaller
				.unmarshal(ClassLoader.getSystemResourceAsStream(path));

		return xmlContent;
	}

}
