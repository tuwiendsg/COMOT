package at.ac.tuwien.dsg.comot.cs;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.ArrayUtils;
import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaInstanceDescription_VM;
import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaMappingProperties;
import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;
import at.ac.tuwien.dsg.comot.rsybl.ObjectFactory;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.requirements.Requirements;

public class UtilsCs {

	protected static final Logger log = LoggerFactory.getLogger(UtilsCs.class);

	protected static ObjectFactory factoryRsybl = new ObjectFactory();

	protected static JAXBContext jaxbContext;

	public static final Class<?>[] CONTEXT_TOSCA = new Class<?>[] { Definitions.class, SalsaMappingProperties.class };
	public static final Class<?>[] CONTEXT_SALSA = new Class<?>[] {
			at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService.class,
			SalsaInstanceDescription_VM.class };

	static {
		try {
			jaxbContext = JAXBContext.newInstance(ArrayUtils.addAll(CONTEXT_TOSCA, CONTEXT_SALSA));
		} catch (JAXBException e) {
			log.error("Failed to create JAXB context", e);
		}
	}

	public static String asString(Definitions definition) throws JAXBException {
		return Utils.asXmlString(definition, CONTEXT_TOSCA);
	}

	public static String asString(at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService cloudService)
			throws JAXBException {
		return Utils.asXmlString(cloudService, CONTEXT_SALSA);
	}

	public static String asString(MonitoredElement element) throws JAXBException {
		return Utils.asXmlString(element);
	}

	public static String asString(DeploymentDescription deployment) throws JAXBException {
		return Utils.asXmlString(deployment);
	}

	public static String asString(CloudServiceXML xml) throws JAXBException {
		return Utils.asXmlString(factoryRsybl.createCloudService(xml), "at.ac.tuwien.dsg.comot.rsybl");
	}

	public static Definitions loadTosca(String path) throws JAXBException, IOException {

		JAXBContext context = JAXBContext.newInstance(CONTEXT_TOSCA);
		Unmarshaller unmarshaller = context.createUnmarshaller();

		return (Definitions) unmarshaller.unmarshal(Utils.loadFileFromSystem(path));
	}

	public static CompositionRulesConfiguration loadMetricCompositionRules(String serviceId, String path)
			throws JAXBException, IOException {

		CompositionRulesConfiguration xmlContent = null;

		JAXBContext context = JAXBContext.newInstance(CompositionRulesConfiguration.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();

		xmlContent = (CompositionRulesConfiguration) unmarshaller
				.unmarshal(Utils.loadFileFromSystem(path));
		xmlContent.setTargetServiceID(serviceId);

		return xmlContent;
	}

	public static MonitoredElement loadMonitoredElement(String path)
			throws JAXBException, IOException {

		JAXBContext context = JAXBContext.newInstance(MonitoredElement.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();

		return (MonitoredElement) unmarshaller.unmarshal(Utils.loadFileFromSystem(path));

	}

	public static Requirements loadRequirements(String path)
			throws JAXBException, IOException {

		JAXBContext context = JAXBContext.newInstance(Requirements.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();

		return (Requirements) unmarshaller.unmarshal(Utils.loadFileFromSystem(path));

	}

}
