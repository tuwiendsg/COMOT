package at.ac.tuwien.dsg.comot.bundles.frontends;

import at.ac.tuwien.dsg.comot.bundles.JsonBundleLoader;
import at.ac.tuwien.dsg.comot.common.model.BundleConfig;
import at.ac.tuwien.dsg.comot.common.model.SoftwareNode;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactReference.ArtifactReference;
import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.SingleScriptArtifact;

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
                .deployedBy(SingleScriptArtifact("id")
                        .withBundleConfig(bundleConfig)
                        .consistsOf(ArtifactReference("tomcat").locatedAt(bundleConfig.getDeploymentConfig().getUri())));
    }
}
