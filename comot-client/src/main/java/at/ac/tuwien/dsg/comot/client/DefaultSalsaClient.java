package at.ac.tuwien.dsg.comot.client;

import at.ac.tuwien.dsg.comot.api.ToscaDescriptionBuilder;
import at.ac.tuwien.dsg.comot.api.ToscaDescriptionBuilderImpl;
import at.ac.tuwien.dsg.comot.common.logging.Markers;
import at.ac.tuwien.dsg.comot.common.model.CloudApplication;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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


        HttpPost method = new HttpPost(configuration.getBaseUri() + configuration.getDeployUri());

        StringBody toscaPart = new StringBody(toscaDescriptionXml, APPLICATION_XML.getMimeType(), Charset.forName("UTF-8"));
        StringBody serviceNamePart = new StringBody(cloudApplication.getName());

        MultipartEntity entity = new MultipartEntity();
        entity.addPart("file", toscaPart);
        entity.addPart("serviceName", serviceNamePart);

        method.setEntity(entity);
        HttpHost endpoint = new HttpHost(configuration.getHost(), configuration.getPort());

        return handleResponse(httpClient.execute(endpoint, method), SalsaClientAction.DEPLOY);
    }


    @Override
    public SalsaResponse undeploy(String serviceId) throws Exception {
        return null;
    }

    @Override
    public SalsaClientConfiguration getConfiguration() {
        return configuration;
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
