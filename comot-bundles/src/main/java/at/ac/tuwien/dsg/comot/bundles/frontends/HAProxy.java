package at.ac.tuwien.dsg.comot.bundles.frontends;

import at.ac.tuwien.dsg.comot.bundles.BundleLoaderFactory;
import at.ac.tuwien.dsg.comot.common.model.BundleConfig;
import at.ac.tuwien.dsg.comot.common.model.SoftwareNode;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactReference.ArtifactReference;
import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.ScriptArtifactTemplate;

/**
 * @author omoser
 */
public class HAProxy extends SoftwareNode {

    protected HAProxy(String id) {
        super(id);
    }

    public static HAProxy HAProxy(String id) {
        BundleConfig bundleConfig = BundleLoaderFactory.getDefaultBundleLoader().getBundleConfig("ha-proxy");
        return (HAProxy) new HAProxy(id)
                .deployedBy(ScriptArtifactTemplate(id)
                        .withBundleConfig(bundleConfig)
                        .consistsOf(ArtifactReference(id).locatedAt(bundleConfig.getDeploymentConfig().getUri())));


    }
}
