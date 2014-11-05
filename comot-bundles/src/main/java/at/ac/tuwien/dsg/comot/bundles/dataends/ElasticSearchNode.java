package at.ac.tuwien.dsg.comot.bundles.dataends;

import at.ac.tuwien.dsg.comot.bundles.BundleLoaderFactory;
import at.ac.tuwien.dsg.comot.common.fluent.BundleConfig;
import at.ac.tuwien.dsg.comot.common.fluent.SoftwareNode;
import static at.ac.tuwien.dsg.comot.common.fluent.ArtifactReference.ArtifactReference;
import static at.ac.tuwien.dsg.comot.common.fluent.ArtifactTemplate.ScriptArtifactTemplate;

/**
 * @author omoser
 */
public class ElasticSearchNode extends SoftwareNode {

    protected ElasticSearchNode(String id) {
        super(id);
    }

    public static ElasticSearchNode ElasticSearchNode(String id) {
        BundleConfig bundleConfig = BundleLoaderFactory.getDefaultBundleLoader().getBundleConfig("elasticsearch");
        return (ElasticSearchNode) new ElasticSearchNode(id)
                .deployedBy(ScriptArtifactTemplate("id")
                        .withBundleConfig(bundleConfig)
                        .consistsOf(ArtifactReference("elasticsearch").locatedAt(bundleConfig.getDeploymentConfig().getUri())));
    }


}
