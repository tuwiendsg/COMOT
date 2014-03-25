package at.ac.tuwien.dsg.comot.client;

import at.ac.tuwien.dsg.comot.api.ToscaDescriptionBuilder;
import at.ac.tuwien.dsg.comot.api.ToscaDescriptionBuilderImpl;
import at.ac.tuwien.dsg.comot.common.logging.Markers;
import at.ac.tuwien.dsg.comot.common.model.CloudApplication;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

import static org.apache.http.entity.ContentType.APPLICATION_XML;

/**
 * @author omoser
 */
public class DefaultSalsaClient implements SalsaClient {

    private static final Logger log = LoggerFactory.getLogger(DefaultSalsaClient.class);

    private final HttpClient httpClient;

    private final SalsaClientConfiguration configuration;

    private final ToscaDescriptionBuilder toscaDescriptionBuilder;

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

    @Override
    public SalsaResponse deploy(CloudApplication cloudApplication) throws Exception {
        String toscaDescriptionXml = toscaDescriptionBuilder.toXml(cloudApplication);

        if (log.isDebugEnabled()) {
            log.debug(Markers.CLIENT, "Deploying cloud application '{}'", cloudApplication.getId());
            log.debug(Markers.CLIENT, "Using configuration '{}'", configuration);
            log.debug(Markers.CLIENT, "TOSCA: {}", toscaDescriptionXml);
        }

        HttpPost method = new HttpPost(configuration.getDeployPath());
        StringBody toscaPart = new StringBody(toscaDescriptionXml, APPLICATION_XML.getMimeType(), Charset.forName("UTF-8"));
        StringBody serviceNamePart = new StringBody(cloudApplication.getName());
        MultipartEntity entity = new MultipartEntity();
        entity.addPart("file", toscaPart);
        entity.addPart("serviceName", serviceNamePart);
        method.setEntity(entity);
        return executeMethod(method, SalsaClientAction.DEPLOY);
    }


    @Override
    public SalsaResponse undeploy(String serviceId) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug(Markers.CLIENT, "Undeploying service with serviceId '{}'", serviceId);
        }

        URI undeployUri = UriBuilder.fromPath(configuration.getUndeployPath()).build(serviceId);
        HttpGet method = new HttpGet(undeployUri);
        return executeMethod(method, SalsaClientAction.UNDEPLOY);
    }

    @Override
    public SalsaResponse spawn(String serviceId, String topologyId, String nodeId, int instanceCount) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug(Markers.CLIENT, "Spawning additional instances (+{}) for serviceId {}, topologyId {} and nodeId {}",
                    instanceCount, serviceId, topologyId, nodeId);
        }

        URI spawnUri = UriBuilder.fromPath(configuration.getSpawnPath()).build(serviceId, topologyId, nodeId, instanceCount);
        HttpGet method = new HttpGet(spawnUri);
        return executeMethod(method, SalsaClientAction.SPAWN);
    }

    @Override
    public SalsaResponse destroy(String serviceId, String topologyId, String nodeId, String instanceId) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug(Markers.CLIENT, "Destroying instance with id {} (service: {} topology: {} node: {})",
                    instanceId, serviceId, topologyId, nodeId);
        }

        URI destroyUri = UriBuilder.fromPath(configuration.getDestroyPath()).build(serviceId, topologyId, nodeId, instanceId);
        HttpGet method = new HttpGet(destroyUri);
        return executeMethod(method, SalsaClientAction.DESTROY);
    }

    @Override
    public SalsaClientConfiguration getConfiguration() {
        return configuration;
    }

    private SalsaResponse executeMethod(HttpRequest method, SalsaClientAction salsaAction) throws IOException {
        HttpHost endpoint = new HttpHost(configuration.getHost(), configuration.getPort());
        return handleResponse(httpClient.execute(endpoint, method), salsaAction);
    }

    protected SalsaResponse handleResponse(HttpResponse response, SalsaClientAction action) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        response.getEntity().writeTo(outputStream);
        String responseBody = new String(outputStream.toByteArray());
        if (log.isDebugEnabled()) {
            log.debug(Markers.CLIENT, "Got HTTP status: {}", response.getStatusLine());
            log.debug(Markers.CLIENT, "Got HTTP response body: {}", responseBody);
        }

        return buildSalsaResponse(action, response.getStatusLine().getStatusCode(), responseBody);

    }

    protected SalsaResponse buildSalsaResponse(SalsaClientAction action, int result, String body) {
        SalsaResponse response = new SalsaResponse()
                .withCode(result)
                .withMessage(body)
                .withExpectedCode(action.expectedResultCode());

        if (action.expectedHttpResultCode != result) {
            log.warn(Markers.CLIENT, "Unexpected result code from Salsa. Expected {} for action {}, but got {}",
                    action.expectedResultCode(), action, result);
        }

        return response;
    }
}
