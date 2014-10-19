package at.ac.tuwien.dsg.comot.core.test;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public class TestUtils {

	private final static Logger log = LoggerFactory.getLogger(TestUtils.class);

	public static CompositionRulesConfiguration loadMetricCompositionRules(String serviceID, String path)
			throws JAXBException, FileNotFoundException {
		
		CompositionRulesConfiguration compositionRules = null;

		JAXBContext context = JAXBContext.newInstance(CompositionRulesConfiguration.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();

		compositionRules = (CompositionRulesConfiguration) unmarshaller.unmarshal(ClassLoader.getSystemResourceAsStream(path));
		compositionRules.setTargetServiceID(serviceID);

		return compositionRules;
	}

	public static String loadFile(String path) throws IOException {
		return IOUtils.toString(ClassLoader.getSystemResourceAsStream(path), "UTF-8");
	}

}
