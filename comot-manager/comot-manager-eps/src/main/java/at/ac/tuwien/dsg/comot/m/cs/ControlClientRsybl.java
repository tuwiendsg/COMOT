package at.ac.tuwien.dsg.comot.m.cs;

import java.util.List;

import javax.annotation.PreDestroy;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import at.ac.tuwien.dsg.comot.m.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.cs.connector.RsyblClient;
import at.ac.tuwien.dsg.comot.m.cs.mapper.DeploymentMapper;
import at.ac.tuwien.dsg.comot.m.cs.mapper.RsyblMapper;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public class ControlClientRsybl implements ControlClient {

	private final Logger log = LoggerFactory.getLogger(ControlClientRsybl.class);

	protected RsyblClient rsybl;
	@Autowired
	protected RsyblMapper rsyblMapper;
	@Autowired
	protected DeploymentMapper deploymentMapper;

	public ControlClientRsybl(RsyblClient rsybl) {
		super();
		this.rsybl = rsybl;
	}

	@Override
	public void sendInitialConfig(
			CloudService service) throws EpsException, JAXBException {

		if (service == null) {
			log.warn("sendInitialConfig(service=null )");
			return;
		}

		String serviceId = service.getId();
		CloudServiceXML cloudServiceXML = rsyblMapper.extractRsybl(service);
		DeploymentDescription deploymentDescription = deploymentMapper.extractDeployment(service);

		rsybl.prepareControl(serviceId);

		rsybl.serviceDescription(serviceId, UtilsCs.asString(cloudServiceXML));

		rsybl.serviceDeployment(serviceId, deploymentDescription);

	}

	@Override
	public void createMcr(String serviceId, CompositionRulesConfiguration compositionRulesConfiguration)
			throws EpsException {
		rsybl.sendMetricsCompositionRules(serviceId, compositionRulesConfiguration);
	}

	@Override
	public void createEffects(String serviceId, String effectsJSON) throws EpsException {
		rsybl.sendElasticityCapabilitiesEffects(serviceId, effectsJSON);
	}

	@Override
	public void updateService(CloudService service) throws EpsException, JAXBException {
		CloudServiceXML cloudServiceXML = rsyblMapper.extractRsybl(service);
		rsybl.updateElasticityRequirements(service.getId(), UtilsCs.asString(cloudServiceXML));
	}

	@Override
	public void updateMcr(String serviceId, CompositionRulesConfiguration compositionRulesConfiguration)
			throws EpsException {
		rsybl.updateMetricsCompositionRules(serviceId, compositionRulesConfiguration);
	}

	@Override
	public void updateEffects(String serviceId, String effectsJSON) throws EpsException {
		rsybl.updateElasticityCapabilitiesEffects(serviceId, effectsJSON);
	}

	@Override
	public void startControl(String serviceId) throws EpsException {
		rsybl.startControl(serviceId);
	}

	@Override
	public void stopControl(String serviceId) throws EpsException {
		rsybl.stopControl(serviceId);
	}

	@PreDestroy
	public void cleanup() {
		if (rsybl != null) {
			log.info("closing rsybl client");
			rsybl.close();
		}
	}

	@Override
	public void setHostAndPort(String host, int port) {
		rsybl.setBaseUri(UriBuilder.fromUri(rsybl.getBaseUri())
				.host(host).port(port).build());
	}

	@Override
	public List<String> listAllServices() throws EpsException {
		return rsybl.listAllServices();
	}

	@Override
	public void removeService(String serviceId) throws EpsException {
		rsybl.removeService(serviceId);

	}

}
