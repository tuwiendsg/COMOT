package at.ac.tuwien.dsg.comot.client;

import at.ac.tuwien.dsg.comot.api.ToscaDescriptionBuilder;
import at.ac.tuwien.dsg.comot.api.ToscaDescriptionBuilderImpl;
import at.ac.tuwien.dsg.comot.common.logging.Markers;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

/**
 * @author omoser
 */
public class DefaultSalsaClient implements SalsaClient {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSalsaClient.class);

    private final HttpClient httpClient;

    private final SalsaClientConfiguration configuration;

    private final ToscaDescriptionBuilder toscaDescriptionBuilder;

    private final ObjectMapper mapper = new ObjectMapper();

    public DefaultSalsaClient() {
        httpClient = new DefaultHttpClient();
        configuration = new SalsaClientConfiguration();
        toscaDescriptionBuilder = new ToscaDescriptionBuilderImpl();
    }

    public DefaultSalsaClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.configuration = new SalsaClientConfiguration();
        this.toscaDescriptionBuilder = new ToscaDescriptionBuilderImpl();
    }

    public DefaultSalsaClient(HttpClient httpClient, SalsaClientConfiguration configuration) {
        this.httpClient = httpClient;
        this.configuration = configuration;
        this.toscaDescriptionBuilder = new ToscaDescriptionBuilderImpl();
    }

    public DefaultSalsaClient(HttpClient httpClient,
            SalsaClientConfiguration configuration,
            ToscaDescriptionBuilder toscaDescriptionBuilder) {

        this.httpClient = httpClient;
        this.configuration = configuration;
        this.toscaDescriptionBuilder = toscaDescriptionBuilder;
    }

    public ToscaDescriptionBuilder getToscaDescriptionBuilder() {
        return toscaDescriptionBuilder;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public SalsaResponse deploy(CloudService CloudService) throws SalsaClientException {
        if (getConfiguration().isValidatingToscaBuilder()) {
            toscaDescriptionBuilder.setValidating(true);
        }

        String toscaDescriptionXml = toscaDescriptionBuilder.toXml(CloudService);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Markers.CLIENT, "Deploying cloud application '{}'", CloudService.getId());
            LOG.debug(Markers.CLIENT, "Using configuration '{}'", configuration);
            LOG.debug(Markers.CLIENT, "TOSCA: {}", toscaDescriptionXml);
        }

        URI deploymentUri = UriBuilder.fromPath(configuration.getDeployPath()).build(CloudService.getName());
        HttpPut method = new HttpPut(deploymentUri);
        method.setEntity(new StringEntity(toscaDescriptionXml, ContentType.APPLICATION_XML));
        return executeMethod(method, SalsaClientAction.DEPLOY);
    }

    @Override
    public SalsaResponse undeploy(String serviceId) throws SalsaClientException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(Markers.CLIENT, "Undeploying service with serviceId '{}'", serviceId);
        }

        URI undeployUri = UriBuilder.fromPath(configuration.getUndeployPath()).build(serviceId);
        HttpDelete method = new HttpDelete(undeployUri);
        return executeMethod(method, SalsaClientAction.UNDEPLOY);
    }

    @Override
    public SalsaResponse spawn(String serviceId, String topologyId, String nodeId, int instanceCount) throws SalsaClientException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(Markers.CLIENT, "Spawning additional instances (+{}) for serviceId {}, topologyId {} and nodeId {}",
                    instanceCount, serviceId, topologyId, nodeId);
        }

        URI spawnUri = UriBuilder.fromPath(configuration.getSpawnPath()).build(serviceId, topologyId, nodeId, instanceCount);
        HttpPost method = new HttpPost(spawnUri);
        return executeMethod(method, SalsaClientAction.SPAWN);
    }

    @Override
    public SalsaResponse destroy(String serviceId, String topologyId, String nodeId, String instanceId) throws SalsaClientException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(Markers.CLIENT, "Destroying instance with id {} (service: {} topology: {} node: {})",
                    instanceId, serviceId, topologyId, nodeId);
        }

        URI destroyUri = UriBuilder.fromPath(configuration.getDestroyPath()).build(serviceId, topologyId, nodeId, instanceId);
        HttpDelete method = new HttpDelete(destroyUri);
        return executeMethod(method, SalsaClientAction.DESTROY);
    }

    @Override
    public SalsaResponse status(String serviceId) throws SalsaClientException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(Markers.CLIENT, "Checking status for serviceId {}", serviceId);
        }

        URI statusUri = UriBuilder.fromPath(configuration.getStatusPath()).build(serviceId);
        HttpGet method = new HttpGet(statusUri);
        return executeMethod(method, SalsaClientAction.STATUS);
    }

    @Override
    public SalsaClientConfiguration getConfiguration() {
        return configuration;
    }

    private SalsaResponse executeMethod(HttpRequest method, SalsaClientAction salsaAction) throws SalsaClientException {
        HttpHost endpoint = new HttpHost(configuration.getHost(), configuration.getPort());
        try {
            return handleResponse(httpClient.execute(endpoint, method), salsaAction);
        } catch (IOException e) {
            LOG.error(Markers.CLIENT, "IOException during SALSA request", e);
            throw new SalsaClientException("IOException during SALSA request", e);
        }
    }

    protected SalsaResponse handleResponse(HttpResponse response, SalsaClientAction action) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        response.getEntity().writeTo(outputStream);
        String responseBody = new String(outputStream.toByteArray());
        if (LOG.isDebugEnabled()) {
            LOG.debug(Markers.CLIENT, "Got HTTP status: {}", response.getStatusLine());
            LOG.debug(Markers.CLIENT, "Got HTTP response body: {}", responseBody);
        }

        return buildSalsaResponse(action, response.getStatusLine().getStatusCode(), responseBody);

    }

    protected SalsaResponse buildSalsaResponse(SalsaClientAction action, int result, String body) throws IOException {
        SalsaResponse response = new SalsaResponse()
                .withCode(result)
                .withMessage(body)
                .withExpectedCode(action.expectedResultCode());

        if (action.expectedHttpResultCode != result) {
            LOG.warn(Markers.CLIENT, "Unexpected result code from Salsa. Expected {} for action {}, but got {}",
                    action.expectedResultCode(), action, result);
        }

        //TODO: make mapping work
//        if (action == SalsaClientAction.STATUS) {
//            //response = new SalsaServiceStatusResponse(response).withExpectedCode(action.expectedResultCode());
//            response = mapper.reader(SalsaServiceStatusResponse.class).readValue(body);
//            response.withCode(result).withExpectedCode(action.expectedResultCode()).withMessage(body);
//        }
        return response;
    }

    public SalsaResponse getServiceDeploymentInfo(String serviceId) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Markers.CLIENT, "Getting deployment information for serviceId {}", serviceId);
        }

        URI statusUri = UriBuilder.fromPath(configuration.getDeploymentInfoPath()).build(serviceId);

        HttpGet method = new HttpGet(statusUri);
        return executeMethod(method, SalsaClientAction.SERVICE_DEPLOYMENT_INFO);

    }

}
