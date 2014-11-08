package at.ac.tuwien.dsg.comot.bundles.frontends;

import at.ac.tuwien.dsg.comot.bundles.JsonBundleLoader;
import at.ac.tuwien.dsg.comot.common.fluent.BundleConfig;
import at.ac.tuwien.dsg.comot.common.fluent.SoftwareNode;
import static at.ac.tuwien.dsg.comot.common.fluent.ArtifactReference.ArtifactReference;
import static at.ac.tuwien.dsg.comot.common.fluent.ArtifactTemplate.ScriptArtifactTemplate;

/**
 * @author omoser
 */
public class TomcatNode extends SoftwareNode {

    protected TomcatNode(String id) {
        super(id);
    }

    public static TomcatNode TomcatNode(String id) {
        BundleConfig bundleConfig = JsonBundleLoader.getInstance().getBundleConfig("tomcat");
        return (TomcatNode) new TomcatNode(id)
                .deployedBy(ScriptArtifactTemplate("id")
                        .withBundleConfig(bundleConfig)
                        .consistsOf(ArtifactReference("tomcat").locatedAt(bundleConfig.getDeploymentConfig().getUri())));
    }
}
