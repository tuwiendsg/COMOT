package at.ac.tuwien.dsg.comot.bundles.frontends;

import at.ac.tuwien.dsg.comot.bundles.BundleLoaderFactory;
import at.ac.tuwien.dsg.comot.common.fluent.BundleConfig;
import at.ac.tuwien.dsg.comot.common.fluent.SoftwareNode;
import static at.ac.tuwien.dsg.comot.common.fluent.ArtifactReference.ArtifactReference;
import static at.ac.tuwien.dsg.comot.common.fluent.ArtifactTemplate.ScriptArtifactTemplate;

/**
 * @author omoser
 */
public class ApacheNode extends SoftwareNode {

    protected ApacheNode(String id) {
        super(id);
    }

    public static ApacheNode ApacheNode(String id) {
        BundleConfig bundleConfig = BundleLoaderFactory.getDefaultBundleLoader().getBundleConfig("apache");
        return (ApacheNode) new ApacheNode(id)
                .deployedBy(ScriptArtifactTemplate(id)
                        .withBundleConfig(bundleConfig)
                        .consistsOf(ArtifactReference(id).locatedAt(bundleConfig.getDeploymentConfig().getUri())));
    }
}
