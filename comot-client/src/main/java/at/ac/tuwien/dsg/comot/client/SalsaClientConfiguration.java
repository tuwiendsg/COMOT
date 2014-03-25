package at.ac.tuwien.dsg.comot.client;

/**
 * @author omoser
 */
public class SalsaClientConfiguration {

    public static final String DEFAULT_HOST = "localhost";

    public static final int DEFAULT_PORT = 8080;

    public static final String DEFAULT_URI = "/salsa";

    private static final String DEFAULT_DEPLOY_URI = "/deploy";

    private static final String DEFAULT_UNDEPLOY_URI = "/undeploy/{serviceId}";

    private static final String DEFAULT_SPAWN_PATH = "/deployInstance/{serviceId}/{topologyId}/{nodeId}/{instanceCount}";

    private static final String DEFAULT_DESTROY_PATH = "/undeployInstance/{serviceId}/{topologyId}/{nodeId}/{instanceId}";

    private String host = DEFAULT_HOST;

    private int port = DEFAULT_PORT;

    private String baseUri = DEFAULT_URI;

    private String deployPath = DEFAULT_DEPLOY_URI;

    private String undeployPath = DEFAULT_UNDEPLOY_URI;

    private String spawnPath = DEFAULT_SPAWN_PATH;

    private String destroyPath = DEFAULT_DESTROY_PATH;

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

    public void setSpawnPath(String spawnPath) {
        this.spawnPath = spawnPath;
    }

    public void setDestroyPath(String destroyPath) {
        this.destroyPath = destroyPath;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public void setDeployPath(String deployPath) {
        this.deployPath = deployPath;
    }

    public void setUndeployPath(String undeployPath) {
        this.undeployPath = undeployPath;
    }

    public String getDeployPath() {
        return baseUri + deployPath;
    }

    public String getUndeployPath() {
        return baseUri + undeployPath;
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
        this.deployPath = deployUri;
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

    public String getSpawnPath() {
        return baseUri + spawnPath;
    }

    public String getDestroyPath() {
        return baseUri + destroyPath;
    }
}
