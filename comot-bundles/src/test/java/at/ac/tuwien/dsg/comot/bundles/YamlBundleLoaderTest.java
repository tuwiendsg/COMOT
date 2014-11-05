package at.ac.tuwien.dsg.comot.bundles;

import at.ac.tuwien.dsg.comot.common.fluent.BundleConfig;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author omoser
 */
public class YamlBundleLoaderTest {

    @Test
    @SuppressWarnings("unchecked")
    public void readScriptArtifacts() throws Exception {
        BundleLoader bundleLoader = YamlBundleLoader.getInstance();
        bundleLoader.init();

        assertTrue(bundleLoader.isAvailable("elasticsearch"));
        assertTrue(bundleLoader.isAvailable("tomcat"));

        BundleConfig elasticsearch = bundleLoader.getBundleConfig("elasticsearch");
        assertEquals("http://134.158.75.65/artifacts/elasticsearch/deploy-es-node.sh", elasticsearch.getDeploymentConfig().getUri());
        assertEquals("latest", elasticsearch.getDeploymentConfig().getVersion());
        assertEquals("-Xmx2g -Xms2g -Des.index.store.type=memory", elasticsearch.getRuntimeConfig().getArguments());
        assertEquals("/var/log/elasticsearch", elasticsearch.getRuntimeConfig().getLoggingConfig().getDir());

        BundleConfig tomcat7 = bundleLoader.getBundleConfig("tomcat");
        assertEquals("http://134.158.75.65/artifacts/tomcat/deploy-tomcat-node.sh", tomcat7.getDeploymentConfig().getUri());
        assertEquals("7.0.53", tomcat7.getDeploymentConfig().getVersion());
        assertEquals("-Dorg.apache.tomcat.util.http.ServerCookie.ALWAYS_ADD_EXPIRES=true", tomcat7.getRuntimeConfig().getArguments());
        assertEquals("-XX:MaxPermSize=256m", tomcat7.getRuntimeConfig().getEnvironment().get("JAVA_OPTS"));
    }
}
