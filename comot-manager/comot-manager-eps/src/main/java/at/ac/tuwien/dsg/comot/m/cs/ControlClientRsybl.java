package at.ac.tuwien.dsg.comot.m.cs;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.cs.connector.RsyblClient;
import at.ac.tuwien.dsg.comot.m.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.RsyblMapper;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
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

		if (service == null) {
			log.warn("sendInitialConfig(service=null )");
			return;
		}

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
	public void createMcr(String serviceId, CompositionRulesConfiguration compositionRulesConfiguration)
			throws CoreServiceException {
		rsybl.sendMetricsCompositionRules(serviceId, compositionRulesConfiguration);
	}

	@Override
	public void createEffects(String serviceId, String effectsJSON) throws CoreServiceException {
		rsybl.sendElasticityCapabilitiesEffects(serviceId, effectsJSON);
	}

	@Override
	public void updateService(CloudService service) throws CoreServiceException {
		CloudServiceXML cloudServiceXML = rsyblMapper.extractRsybl(service);
		rsybl.updateElasticityRequirements(service.getId(), cloudServiceXML);
	}

	@Override
	public void updateMcr(String serviceId, CompositionRulesConfiguration compositionRulesConfiguration)
			throws CoreServiceException {
		rsybl.updateMetricsCompositionRules(serviceId, compositionRulesConfiguration);
	}

	@Override
	public void updateEffects(String serviceId, String effectsJSON) throws CoreServiceException {
		rsybl.updateElasticityCapabilitiesEffects(serviceId, effectsJSON);
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
