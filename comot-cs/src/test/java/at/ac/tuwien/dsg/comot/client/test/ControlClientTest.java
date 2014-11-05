package at.ac.tuwien.dsg.comot.client.test;

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

import org.junit.Test;

import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.test.samples.DataAsAServiceCloudApplication;
import at.ac.tuwien.dsg.comot.common.test.samples.ExampleDeployOneVM;
import at.ac.tuwien.dsg.comot.cs.transformer.ToscaDescriptionBuilderImpl;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public class ControlClientTest extends AbstractTest {

	@Test
	public void aaa() throws CoreServiceException {
		deployment.deploy(ExampleDeployOneVM.build());
	}

	@Test
	public void testJaxb() throws CoreServiceException, JAXBException, IOException {

		CompositionRulesConfiguration mcr = TestUtils.loadMetricCompositionRules("aaaa",
				"./mela/defCompositionRules.xml");

		UnifiedConfiguration unified = new UnifiedConfiguration();
		unified.setEffects("effects");
		unified.setMcr(mcr);

		try {

			StringWriter sw = new StringWriter();

			JAXBContext jaxbContext = JAXBContext.newInstance(UnifiedConfiguration.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(unified, sw);
			// jaxbMarshaller.marshal(unified, System.out);

			String xml = sw.toString();
			log.info(xml);

			StringReader reader = new StringReader(xml);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			UnifiedConfiguration customer = (UnifiedConfiguration) jaxbUnmarshaller.unmarshal(reader);

			log.info("" + customer);

			sw = new StringWriter();
			jaxbMarshaller.marshal(customer.getMcr(), sw);
			log.info(sw.toString());

		} catch (JAXBException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testTosca() throws FileNotFoundException, JAXBException {

		TDefinitions tosca = loadTosca("./ExampleExecutableOnVM.xml");

		TServiceTemplate serviceTemplate = (TServiceTemplate) tosca
				.getServiceTemplateOrNodeTypeOrNodeTypeImplementation().get(0);
		TNodeTemplate nodeTemplate = (TNodeTemplate) serviceTemplate.getTopologyTemplate()
				.getNodeTemplateOrRelationshipTemplate().get(0);

		log.info("{}", Utils.asXmlString(tosca));

		log.info("{}", nodeTemplate.getType());

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

	@Test
	public void gdgdfgdfg() {

		String xml = new ToscaDescriptionBuilderImpl().toXml(DataAsAServiceCloudApplication.build());
		System.out.println(xml);

	}
}
