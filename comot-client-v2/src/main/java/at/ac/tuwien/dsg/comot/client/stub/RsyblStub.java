package at.ac.tuwien.dsg.comot.client.stub;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.logging.Markers;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.CloudServiceXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public class RsyblStub extends CoreServiceStub {

	private final Logger log = LoggerFactory.getLogger(RsyblStub.class);

	protected static final String DEF_BASE_PATH = "/rSYBL/restWS";

	protected static final String PREPARE_CONTROL_PATH = "{id}/prepareControl";
	protected static final String SERV_DESCRIPTION_PATH = "{id}/description";
	protected static final String SERV_DEPLOYMENT_PATH = "{id}/deployment";
	protected static final String MCR_PATH = "{id}/compositionRules";
	protected static final String ECE_PATH = "{id}/elasticityCapabilitiesEffects";
	protected static final String START_CONTROL_PATH = "{id}/startControl";
	protected static final String STOP_CONTROL_PATH = "{id}/stopControl";
	protected static final String REPLACE_REQUIREMENTS_PATH = "{id}/elasticityRequirements/xml";

	public RsyblStub() {
		this(DEF_HOST, DEF_PORT);
	}

	public RsyblStub(String host) {
		this(host, DEF_PORT, DEF_BASE_PATH);
	}

	public RsyblStub(String host, int port) {
		this(host, port, DEF_BASE_PATH);
	}

	public RsyblStub(String host, int port, String basePath) {
		super(host, port, basePath);
	}

	public void prepareControl(String serviceId) throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(PREPARE_CONTROL_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.put(Entity.xml(""));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "prepareControl '{}'. Response: '{}'", serviceId,
					msg);
		}

	}

	public void serviceDescription(String serviceId, CloudServiceXML cloudServiceXML) throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(SERV_DESCRIPTION_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.xml(cloudServiceXML));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "serviceDescription '{}'. Response: '{}'", serviceId,
					msg);
		}

	}

	public void serviceDeployment(String serviceId, DeploymentDescription deploymentDescription)
			throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(SERV_DEPLOYMENT_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.xml(deploymentDescription));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "serviceDeployment '{}'. Response: '{}'", serviceId,
					msg);
		}
	}

	public void sendMetricsCompositionRules(String serviceId, CompositionRulesConfiguration compositionRulesConfiguration)
			throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(MCR_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.xml(compositionRulesConfiguration));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "metricsCompositionRules '{}'. Response: '{}'", serviceId,
					msg);
		}
	}

	public void updateMetricsCompositionRules(String serviceId,
			CompositionRulesConfiguration compositionRulesConfiguration)
			throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(MCR_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.post(Entity.xml(compositionRulesConfiguration));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "updateMetricsCompositionRules '{}'. Response: '{}'", serviceId,
					msg);
		}
	}

	public void sendElasticityCapabilitiesEffects(String serviceId, String effectsJSON) throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(ECE_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.json(effectsJSON));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "elasticityCapabilitiesEffects '{}'. Response: '{}'", serviceId,
					msg);
		}
	}

	public void updateElasticityCapabilitiesEffects(String serviceId, String effectsJSON) throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(ECE_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.post(Entity.json(effectsJSON));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "updateElasticityCapabilitiesEffects '{}'. Response: '{}'", serviceId,
					msg);
		}
	}

	public void startControl(String serviceId) throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(START_CONTROL_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.xml(""));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "startControl '{}'. Response: '{}'", serviceId,
					msg);
		}

	}

	public void stopControl(String serviceId) throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(STOP_CONTROL_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.xml(""));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "stopControl '{}'. Response: '{}'", serviceId,
					msg);
		}
	}

	/**
	 * = replaceRequirements
	 * @param serviceId
	 * @param cloudServiceXML
	 * @throws CoreServiceException
	 */
	public void updateElasticityRequirements(String serviceId, CloudServiceXML cloudServiceXML)
			throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(REPLACE_REQUIREMENTS_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.post(Entity.xml(cloudServiceXML));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "updateElasticityRequirements '{}'. Response: '{}'", serviceId,
					msg);
		}
	}

}
