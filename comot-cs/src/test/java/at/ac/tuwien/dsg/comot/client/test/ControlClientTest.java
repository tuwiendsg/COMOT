package at.ac.tuwien.dsg.comot.client.test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.test.samples.ExampleDeployOneVM;
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
			//jaxbMarshaller.marshal(unified, System.out);
			
			String xml = sw.toString();
			log.info(xml);
			
			StringReader reader = new StringReader(xml);
			
			
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			UnifiedConfiguration customer = (UnifiedConfiguration) jaxbUnmarshaller.unmarshal(reader);

			log.info(""+customer);
			
			sw = new StringWriter();
			jaxbMarshaller.marshal(customer.getMcr(), sw);
			log.info(sw.toString());
			
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		

	}
}
