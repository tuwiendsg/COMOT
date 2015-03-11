package at.ac.tuwien.dsg.comot.m.common.coreservices;

import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public interface ControlClient extends CoreServiceClient {

	public void sendInitialConfig(
			CloudService service) throws CoreServiceException;

	public void createMcr(String serviceId, CompositionRulesConfiguration compositionRulesConfiguration)
			throws CoreServiceException;

	public void createEffects(String serviceId, String effectsJSON)
			throws CoreServiceException;

	public void startControl(
			String serviceId) throws CoreServiceException;

	public void stopControl(
			String serviceId) throws CoreServiceException;

	public void updateEffects(String serviceId, String effectsJSON)
			throws CoreServiceException;

	public void updateMcr(String serviceId, CompositionRulesConfiguration compositionRulesConfiguration)
			throws CoreServiceException;

	public void updateService(CloudService service)
			throws CoreServiceException;

}
