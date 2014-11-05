package at.ac.tuwien.dsg.comot.cs.connector;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService;
import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.logging.Markers;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

public class SalsaClient extends CoreServiceClient {

	private final Logger log = LoggerFactory.getLogger(SalsaClient.class);

	protected static final String DEF_BASE_PATH = "/salsa-engine/rest";

	protected static final String DEPLOY_PATH = "services/xml";
	protected static final String UNDEPLOY_PATH = "services/{serviceId}";
	protected static final String SPAWN_PATH = "services/{serviceId}/topologies/{topologyId}/nodes/{nodeId}/instance-count/{instanceCount}";
	protected static final String DESTROY_PATH = "services/{serviceId}/topologies/{topologyId}/nodes/{nodeId}/instances/{instanceId}";
	protected static final String STATUS_PATH = "services/{serviceId}";
	protected static final String DEPLOYMENT_INFO_PATH = "services/tosca/{serviceId}/sybl";

	public SalsaClient() {
		this(DEF_HOST, DEF_PORT);
	}

	public SalsaClient(String host) {
		this(host, DEF_PORT, DEF_BASE_PATH);
	}

	public SalsaClient(String host, int port) {
		this(host, port, DEF_BASE_PATH);
	}

	public SalsaClient(String host, int port, String basePath) {
		super(host, port, basePath);
	}

	public String deploy(String toscaDescriptionXml) throws CoreServiceException {

		if (log.isDebugEnabled()) {
			log.debug(Markers.CLIENT, "Deploying cloud application");
			log.debug(Markers.CLIENT, "TOSCA: {}", toscaDescriptionXml);
		}

		Response response = client.target(getBaseUri())
				.path(DEPLOY_PATH)
				.request(MediaType.APPLICATION_XML)
				.put(Entity.xml(toscaDescriptionXml));

		processResponseStatus(response);

		String serviceId = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "Successfully deployed. Response: '{}'",
					serviceId);
		}

		return serviceId;

	}

	public void undeploy(String serviceId) throws CoreServiceException {

		if (log.isDebugEnabled()) {
			log.debug(Markers.CLIENT, "Undeploying service with serviceId '{}'", serviceId);
		}

		Response response = client.target(getBaseUri())
				.path(UNDEPLOY_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.TEXT_XML)
				// .header("Accept", MediaType.TEXT_XML)
				.delete();

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "Successfully undeployed '{}'. Response: '{}'", serviceId,
					msg);
		}
	}

	public void spawn(String serviceId, String topologyId, String nodeId, int instanceCount)
			throws CoreServiceException {

		if (log.isDebugEnabled()) {
			log.debug(Markers.CLIENT,
					"Spawning additional instances (+{}) for serviceId={}, topologyId={}, nodeId={}",
					instanceCount, serviceId, topologyId, nodeId);
		}

		Response response = client.target(getBaseUri())
				.path(SPAWN_PATH)
				.resolveTemplate("serviceId", serviceId)
				.resolveTemplate("topologyId", topologyId)
				.resolveTemplate("nodeId", nodeId)
				.resolveTemplate("instanceCount", instanceCount)
				.request(MediaType.TEXT_XML)
				// .header("Accept", MediaType.TEXT_XML)
				.post(Entity.text(""));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(
					Markers.CLIENT,
					"Successfully spawned additional instances (+{}) for serviceId={}, topologyId={}, nodeId={}. Response: '{}'",
					instanceCount, serviceId, topologyId, nodeId, msg);
		}
	}

	public void destroy(String serviceId, String topologyId, String nodeId, int instanceId) throws CoreServiceException {

		if (log.isDebugEnabled()) {
			log.debug(Markers.CLIENT, "Destroying instance with id {} (service: {} topology: {} node: {})",
					instanceId, serviceId, topologyId, nodeId);
		}

		Response response = client.target(getBaseUri())
				.path(DESTROY_PATH)
				.resolveTemplate("serviceId", serviceId)
				.resolveTemplate("topologyId", topologyId)
				.resolveTemplate("nodeId", nodeId)
				.resolveTemplate("instanceId", instanceId)
				.request(MediaType.TEXT_XML)
				// .header("Accept", MediaType.TEXT_XML)
				.delete();

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT,
					"Successfully destroyed instance with id {} (service={}, topology={}, node={}). Response: '{}'",
					instanceId, serviceId, topologyId, nodeId, msg);
		}
	}

	public CloudService getStatus(String serviceId)
			throws CoreServiceException {

		if (log.isDebugEnabled()) {
			log.debug(Markers.CLIENT, "Checking status for serviceId {}", serviceId);
		}

		Response response = client.target(getBaseUri())
				.path(STATUS_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.TEXT_XML)
				// .header("Accept", MediaType.TEXT_XML)
				.get();

		processResponseStatus(response);

		at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService service = response
				.readEntity(at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "Successfully checked status for serviceId '{}'. Response: '{}'",
					serviceId, service);
		}
		return service;

	}

	public DeploymentDescription getServiceDeploymentInfo(String serviceId) throws CoreServiceException {

		if (log.isDebugEnabled()) {
			log.debug(Markers.CLIENT, "Getting DeploymentInfo for serviceId {}", serviceId);
		}

		Response response = client.target(getBaseUri())
				.path(DEPLOYMENT_INFO_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.TEXT_XML)
				// .header("Accept", MediaType.TEXT_XML)
				.get();

		processResponseStatus(response);

		DeploymentDescription description = response.readEntity(DeploymentDescription.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "Successfully got DeploymentInfo for serviceId '{}'. Response: '{}'",
					serviceId, description);
		}
		return description;
	}

	// TODO only temporary
	public String getStatusGui(String serviceId) throws CoreServiceException {

		if (log.isDebugEnabled()) {
			log.debug(Markers.CLIENT, "getStatusGui for serviceId {}", serviceId);
		}

		Response response = client.target(getBaseUri())
				.path("/viewgenerator/cloudservice/json/compact/{serviceId}")
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.TEXT_PLAIN)
				// .header("Accept", MediaType.TEXT_XML)
				.get();

		processResponseStatus(response);

		String service = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "Successfully checked getStatusGui for serviceId '{}'. Response: '{}'",
					serviceId, service);
		}
		return service;

	}

}
