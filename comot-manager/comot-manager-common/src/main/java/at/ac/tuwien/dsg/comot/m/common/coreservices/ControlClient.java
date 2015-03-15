package at.ac.tuwien.dsg.comot.m.common.coreservices;

import java.util.List;

import javax.xml.bind.JAXBException;

import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public interface ControlClient extends ElasticPlatformServiceClient {

	public void sendInitialConfig(
			CloudService service) throws EpsException, JAXBException;

	public void createMcr(String serviceId, CompositionRulesConfiguration compositionRulesConfiguration)
			throws EpsException;

	public void createEffects(String serviceId, String effectsJSON)
			throws EpsException;

	public void startControl(
			String serviceId) throws EpsException;

	public void stopControl(
			String serviceId) throws EpsException;

	public void updateEffects(String serviceId, String effectsJSON) throws EpsException;

	public void updateMcr(String serviceId, CompositionRulesConfiguration compositionRulesConfiguration)
			throws EpsException;

	public void updateService(CloudService service) throws EpsException, JAXBException;

	public List<String> listAllServices() throws EpsException;

	public void removeService(String serviceId) throws EpsException;
}
