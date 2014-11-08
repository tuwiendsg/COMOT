package at.ac.tuwien.dsg.comot.common.coreservices;

import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public interface ControlClient extends CoreServiceClient {

	public void sendInitialConfig(
			CloudService service,
			DeploymentDescription deploymentDescription,
			CompositionRulesConfiguration compositionRulesConfiguration,
			String effectsJSON) throws CoreServiceException;

	public void sendUpdatedConfig(
			CloudService service,
			CompositionRulesConfiguration compositionRulesConfiguration,
			String effectsJSON) throws CoreServiceException;

	public void startControl(
			String serviceId) throws CoreServiceException;

	public void stopControl(
			String serviceId) throws CoreServiceException;

}
