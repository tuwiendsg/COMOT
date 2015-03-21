package at.ac.tuwien.dsg.comot.m.cs.connector;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public class RsyblClient extends CoreServiceClient {

	private final Logger log = LoggerFactory.getLogger(RsyblClient.class);

	protected static final String DEF_BASE_PATH = "http://127.0.0.1:8280/rSYBL/restWS";

	protected static final String PREPARE_CONTROL_PATH = "{id}/prepareControl";
	protected static final String SERV_DESCRIPTION_PATH = "{id}/description";
	protected static final String SERV_DEPLOYMENT_PATH = "{id}/deployment";
	protected static final String MCR_PATH = "{id}/compositionRules";
	protected static final String ECE_PATH = "{id}/elasticityCapabilitiesEffects";
	protected static final String START_CONTROL_PATH = "{id}/startControl";
	protected static final String STOP_CONTROL_PATH = "{id}/stopControl";
	protected static final String REPLACE_REQUIREMENTS_PATH = "{id}/elasticityRequirements/xml";
	protected static final String LIST_ALL_SERVICES_PATH = "elasticservices";
	protected static final String REMOVE_PATH = "managedService/{id}";

	public RsyblClient() throws URISyntaxException {
		this(new URI(DEF_BASE_PATH));
	}

	public RsyblClient(URI baseUri) {
		super("rSYBL", baseUri);
	}

	public void prepareControl(String serviceId) throws EpsException {

		Response response = client.target(getBaseUri())
				.path(PREPARE_CONTROL_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.xml(""));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		log.info(ln + "prepareControl '{}'. Response: '{}'", serviceId, msg);
	}

	public void serviceDescription(String serviceId, String cloudServiceXML) throws EpsException {

		log.trace("serviceDescription {}", cloudServiceXML);

		Response response = client.target(getBaseUri())
				.path(SERV_DESCRIPTION_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.xml(cloudServiceXML));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		log.info(ln + "serviceDescription '{}'. Response: '{}'", serviceId, msg);
	}

	public void serviceDeployment(String serviceId, DeploymentDescription deploymentDescription)
			throws EpsException {

		try {
			log.trace("serviceDeployment {}", UtilsCs.asString(deploymentDescription));
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		Response response = client.target(getBaseUri())
				.path(SERV_DEPLOYMENT_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.xml(deploymentDescription));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		log.info(ln + "serviceDeployment '{}'. Response: '{}'", serviceId, msg);
	}

	public void sendMetricsCompositionRules(String serviceId,
			CompositionRulesConfiguration compositionRulesConfiguration)
			throws EpsException {

		Response response = client.target(getBaseUri())
				.path(MCR_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.xml(compositionRulesConfiguration));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		log.info(ln + "metricsCompositionRules '{}'. Response: '{}'", serviceId, msg);
	}

	public void updateMetricsCompositionRules(String serviceId,
			CompositionRulesConfiguration compositionRulesConfiguration)
			throws EpsException {

		Response response = client.target(getBaseUri())
				.path(MCR_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.post(Entity.xml(compositionRulesConfiguration));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		log.info(ln + "updateMetricsCompositionRules '{}'. Response: '{}'", serviceId, msg);
	}

	public void sendElasticityCapabilitiesEffects(String serviceId, String effectsJSON) throws EpsException {

		Response response = client.target(getBaseUri())
				.path(ECE_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.json(effectsJSON));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		log.info(ln + "elasticityCapabilitiesEffects '{}'. Response: '{}'", serviceId, msg);
	}

	public void updateElasticityCapabilitiesEffects(String serviceId, String effectsJSON) throws EpsException {

		Response response = client.target(getBaseUri())
				.path(ECE_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.post(Entity.json(effectsJSON));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		log.info(ln + "updateElasticityCapabilitiesEffects '{}'. Response: '{}'", serviceId, msg);
	}

	public void startControl(String serviceId) throws EpsException {

		Response response = client.target(getBaseUri())
				.path(START_CONTROL_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.xml(""));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		log.info(ln + "startControl '{}'. Response: '{}'", serviceId, msg);
	}

	public void stopControl(String serviceId) throws EpsException {

		Response response = client.target(getBaseUri())
				.path(STOP_CONTROL_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.xml(""));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		log.info(ln + "stopControl '{}'. Response: '{}'", serviceId, msg);
	}

	/**
	 * = replaceRequirements
	 * 
	 * @param serviceId
	 * @param cloudServiceXML
	 * @throws EpsException
	 */
	public void updateElasticityRequirements(String serviceId, String cloudServiceXML)
			throws EpsException {

		Response response = client.target(getBaseUri())
				.path(REPLACE_REQUIREMENTS_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.post(Entity.xml(cloudServiceXML));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		log.info(ln + "updateElasticityRequirements '{}'. Response: '{}'", serviceId, msg);
	}

	public List<String> listAllServices() throws EpsException {

		Response response = client.target(getBaseUri())
				.path(LIST_ALL_SERVICES_PATH)
				.request(MediaType.TEXT_PLAIN)
				.get();

		processResponseStatus(response);

		String result = response.readEntity(String.class);

		log.info(ln + "listAllServices . Response: '{}'", result);

		Arrays.asList(result.split(","));

		return Arrays.asList(result.split(","));
	}

	public void removeService(String serviceId) throws EpsException {

		Response response = client.target(getBaseUri())
				.path(REMOVE_PATH)
				.resolveTemplate("id", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.delete();

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		log.info(ln + "updateElasticityRequirements '{}'. Response: '{}'", serviceId, msg);
	}

}
