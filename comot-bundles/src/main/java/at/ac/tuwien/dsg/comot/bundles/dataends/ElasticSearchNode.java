package at.ac.tuwien.dsg.comot.bundles.dataends;

import at.ac.tuwien.dsg.comot.bundles.BundleLoaderFactory;
import at.ac.tuwien.dsg.comot.common.model.BundleConfig;
import at.ac.tuwien.dsg.comot.common.model.SoftwareNode;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactReference.ArtifactReference;
import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.ScriptArtifactTemplate;

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
