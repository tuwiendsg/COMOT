package at.ac.tuwien.dsg.comot.client;

/**
 * @author omoser
 */
public class SalsaClientConfiguration {

    public static final String DEFAULT_HOST = "localhost";

    public static final int DEFAULT_PORT = 8080;

    public static final String DEFAULT_URI = "/salsa";

    private static final String DEFAULT_DEPLOY_URI = "/deploy";

    private static final String DEFAULT_UNDEPLOY_URI = "/undeploy";

    private String host = DEFAULT_HOST;

    private int port = DEFAULT_PORT;

    private String baseUri = DEFAULT_URI;

    private String deployUri = DEFAULT_DEPLOY_URI;

    private String unDeployUri = DEFAULT_UNDEPLOY_URI;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getBaseUri() {
        return baseUri;
    }


    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public void setDeployUri(String deployUri) {
        this.deployUri = deployUri;
    }

    public void setUnDeployUri(String unDeployUri) {
        this.unDeployUri = unDeployUri;
    }

    public String getDeployUri() {
        return deployUri;
    }

    public String getUnDeployUri() {
        return unDeployUri;
    }

    public SalsaClientConfiguration withHost(final String host) {
        this.host = host;
        return this;
    }

    public SalsaClientConfiguration withPort(final int port) {
        this.port = port;
        return this;
    }

    public SalsaClientConfiguration withUri(final String uri) {
        this.baseUri = uri;
        return this;
    }

    public SalsaClientConfiguration withDeployUri(final String deployUri) {
        this.deployUri = deployUri;
        return this;
    }


    @Override
    public String toString() {
        return "SalsaClientConfiguration{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", baseUri='" + baseUri + '\'' +
                '}';
    }

}
