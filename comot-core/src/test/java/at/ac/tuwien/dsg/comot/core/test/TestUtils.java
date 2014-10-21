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
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.requirements.Requirements;

public class TestUtils {

	private final static Logger log = LoggerFactory.getLogger(TestUtils.class);

	public static CompositionRulesConfiguration loadMetricCompositionRules(String serviceId, String path)
			throws JAXBException, FileNotFoundException {

		CompositionRulesConfiguration xmlContent = null;

		JAXBContext context = JAXBContext.newInstance(CompositionRulesConfiguration.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();

		xmlContent = (CompositionRulesConfiguration) unmarshaller
				.unmarshal(ClassLoader.getSystemResourceAsStream(path));
		xmlContent.setTargetServiceID(serviceId);

		return xmlContent;
	}

	public static MonitoredElement loadMonitoredElement(String path)
			throws JAXBException, FileNotFoundException {

		JAXBContext context = JAXBContext.newInstance(MonitoredElement.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();

		return (MonitoredElement) unmarshaller.unmarshal(ClassLoader.getSystemResourceAsStream(path));

	}
	
	public static Requirements loadRequirements(String path)
			throws JAXBException, FileNotFoundException {

		JAXBContext context = JAXBContext.newInstance(Requirements.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();

		return (Requirements) unmarshaller.unmarshal(ClassLoader.getSystemResourceAsStream(path));

	}

	public static String loadFile(String path) throws IOException {
		return IOUtils.toString(ClassLoader.getSystemResourceAsStream(path), "UTF-8");
	}

}
