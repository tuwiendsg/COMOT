package at.ac.tuwien.dsg.comot.cs;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.cs.connector.RsyblClient;
import at.ac.tuwien.dsg.comot.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.cs.mapper.RsyblMapper;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public class ControlClientRsybl implements ControlClient {

	private final Logger log = LoggerFactory.getLogger(ControlClientRsybl.class);

	@Autowired
	protected RsyblClient rsybl;
	@Autowired
	protected RsyblMapper rsyblMapper;
	@Autowired
	protected DeploymentMapper deploymentMapper;

	@Override
	public void sendInitialConfig(
			CloudService service,
			CompositionRulesConfiguration compositionRulesConfiguration,
			String effectsJSON) throws CoreServiceException {

		String serviceId = service.getId();
		CloudServiceXML cloudServiceXML = rsyblMapper.extractRsybl(service);
		DeploymentDescription deploymentDescription = deploymentMapper.extractDeployment(service);

		rsybl.prepareControl(serviceId);

		rsybl.serviceDescription(serviceId, cloudServiceXML);

		rsybl.serviceDeployment(serviceId, deploymentDescription);

		if (compositionRulesConfiguration != null) {// optional
			rsybl.sendMetricsCompositionRules(serviceId, compositionRulesConfiguration);
		}

		if (effectsJSON != null) {// optional
			rsybl.sendElasticityCapabilitiesEffects(serviceId, effectsJSON);
		}

	}

	@Override
	public void sendUpdatedConfig(
			CloudService service,
			CompositionRulesConfiguration compositionRulesConfiguration,
			String effectsJSON) throws CoreServiceException {

		String serviceId = service.getId();
		CloudServiceXML cloudServiceXML = rsyblMapper.extractRsybl(service);

		rsybl.updateMetricsCompositionRules(serviceId, compositionRulesConfiguration);

		rsybl.updateElasticityCapabilitiesEffects(serviceId, effectsJSON);

		rsybl.updateElasticityRequirements(serviceId, cloudServiceXML);

	}

	@Override
	public void startControl(String serviceId) throws CoreServiceException {
		rsybl.startControl(serviceId);
	}

	@Override
	public void stopControl(String serviceId) throws CoreServiceException {
		rsybl.stopControl(serviceId);
	}

	@PreDestroy
	public void cleanup() {
		log.info("closing rsybl client");
		rsybl.close();
	}

	@Override
	public void setHost(String host) {
		rsybl.setHost(host);
	}

	@Override
	public void setPort(int port) {
		rsybl.setPort(port);
	}

}
