package at.ac.tuwien.dsg.comot.cs.connector;

import java.io.StringReader;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService;
import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaInstanceDescription_VM;
import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaMappingProperties;
import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

public class SalsaClient extends CoreServiceClient {

	private final Logger log = LoggerFactory.getLogger(SalsaClient.class);

	protected static final String DEF_BASE_PATH = "/salsa-engine/rest";

	protected static final String DEPLOY_PATH = "services/xml";
	protected static final String UNDEPLOY_PATH = "services/{serviceId}";
	protected static final String SPAWN_PATH = "services/{serviceId}/topologies/{topologyId}/nodes/{nodeId}/instance-count/{instanceCount}";
	protected static final String DESTROY_PATH = "services/{serviceId}/topologies/{topologyId}/nodes/{nodeId}/instances/{instanceId}";
	protected static final String STATUS_PATH = "services/{serviceId}";
	protected static final String TOSCA_PATH = "services/tosca/{serviceId}";
	protected static final String DEPLOYMENT_INFO_PATH = "services/tosca/{serviceId}/sybl";
	protected static final String SERVICES_LIST = "viewgenerator/cloudservice/json/list";

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
		name = "[SALSA] ";
	}

	public String deploy(String toscaDescriptionXml) throws CoreServiceException {

		log.trace(name + "Deploying cloud application: {}", toscaDescriptionXml);

		Response response = client.target(getBaseUri())
				.path(DEPLOY_PATH)
				.request(MediaType.APPLICATION_XML)
				.put(Entity.xml(toscaDescriptionXml));

		processResponseStatus(response);

		String serviceId = response.readEntity(String.class);

		log.info(name + "deployed service. Response: '{}'",
				serviceId);

		return serviceId;

	}

	public void undeploy(String serviceId) throws CoreServiceException {

		log.trace(name + "Undeploying service with serviceId '{}'", serviceId);

		Response response = client.target(getBaseUri())
				.path(UNDEPLOY_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.TEXT_XML)
				.delete();

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		log.info(name + "undeployed '{}'. Response: '{}'", serviceId, msg);

	}

	public void spawn(String serviceId, String topologyId, String nodeId, int instanceCount)
			throws CoreServiceException {

		log.trace(name +
				"Spawning additional instances (+{}) for serviceId={}, topologyId={}, nodeId={}",
				instanceCount, serviceId, topologyId, nodeId);

		Response response = client.target(getBaseUri())
				.path(SPAWN_PATH)
				.resolveTemplate("serviceId", serviceId)
				.resolveTemplate("topologyId", topologyId)
				.resolveTemplate("nodeId", nodeId)
				.resolveTemplate("instanceCount", instanceCount)
				.request(MediaType.TEXT_XML)
				.post(Entity.text(""));

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		log.info(
				name + "Spawned additional instances (+{}) for serviceId={}, topologyId={}, nodeId={}. Response: '{}'",
				instanceCount, serviceId, topologyId, nodeId, msg);

	}

	public void destroy(String serviceId, String topologyId, String nodeId, int instanceId) throws CoreServiceException {

		log.trace(name + "Destroying instance with id {} (service: {} topology: {} node: {})",
				instanceId, serviceId, topologyId, nodeId);

		Response response = client.target(getBaseUri())
				.path(DESTROY_PATH)
				.resolveTemplate("serviceId", serviceId)
				.resolveTemplate("topologyId", topologyId)
				.resolveTemplate("nodeId", nodeId)
				.resolveTemplate("instanceId", instanceId)
				.request(MediaType.TEXT_XML)
				.delete();

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		log.info(name + "Sestroyed instance with id {} (service={}, topology={}, node={}). Response: '{}'",
				instanceId, serviceId, topologyId, nodeId, msg);

	}

	public CloudService getStatus(String serviceId)
			throws CoreServiceException, ComotException {

		log.trace(name + "Checking status for serviceId {}", serviceId);

		Response response = client.target(getBaseUri())
				.path(STATUS_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.TEXT_XML)
				.get();

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		try (StringReader reader = new StringReader(msg)) {

			JAXBContext jaxbContext = JAXBContext.newInstance(CloudService.class, SalsaInstanceDescription_VM.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			CloudService service = (CloudService) jaxbUnmarshaller.unmarshal(reader);

			log.info(name + "Checked status for serviceId '{}'. Response: '{}'", serviceId, service);

			return service;

		} catch (JAXBException e) {
			throw new ComotException("Failed to unmarshall response into JAXB status CloudService", e);
		}

	}

	public Definitions getTosca(String serviceId)
			throws CoreServiceException, ComotException {

		log.trace(name + "Getting tosca for serviceId {}", serviceId);

		Response response = client.target(getBaseUri())
				.path(TOSCA_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.TEXT_XML)
				.get();

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		try (StringReader reader = new StringReader(msg)) {

			JAXBContext jaxbContext = JAXBContext.newInstance(Definitions.class, SalsaMappingProperties.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Definitions service = (Definitions) jaxbUnmarshaller.unmarshal(reader);

			log.info(name + "Tosca for serviceId '{}'. Response: '{}'", serviceId, service);

			return service;

		} catch (JAXBException e) {
			throw new ComotException("Failed to unmarshall response into JAXB TOSCA", e);
		}

	}

	/**
	 * Use {@link #getStatus(String) getStatus} instead
	 * 
	 * @param serviceId
	 * @return
	 * @throws CoreServiceException
	 */
	@Deprecated
	public DeploymentDescription getServiceDeploymentInfo(String serviceId) throws CoreServiceException {

		log.trace(name + "Getting DeploymentInfo for serviceId {}", serviceId);

		Response response = client.target(getBaseUri())
				.path(DEPLOYMENT_INFO_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.TEXT_XML)
				// .header("Accept", MediaType.TEXT_XML)
				.get();

		processResponseStatus(response);

		DeploymentDescription description = response.readEntity(DeploymentDescription.class);

		log.info(name + "DeploymentInfo for serviceId '{}'. Response: '{}'", serviceId, description);

		return description;
	}

	@Deprecated
	public String getServices() throws CoreServiceException {

		log.trace(name + "Getting list of all services {}");

		Response response = client.target(getBaseUri())
				.path(SERVICES_LIST)
				.request(MediaType.TEXT_PLAIN)
				.get();

		processResponseStatus(response);

		String msg = response.readEntity(String.class);

		log.info(name + "List of all services. Response: '{}'", msg);

		return msg;
	}

}
