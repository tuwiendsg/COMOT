package at.ac.tuwien.dsg.comot.bundles.nodes;

import at.ac.tuwien.dsg.comot.bundles.JsonBundleLoader;
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
        BundleConfig bundleConfig = JsonBundleLoader.getInstance().getBundleConfig("elasticsearch");

        return (ElasticSearchNode) new ElasticSearchNode(id)
                .deployedBy(ScriptArtifactTemplate("id")
                        .withBundleConfig(bundleConfig)
                        .consistsOf(ArtifactReference("elasticsearch").locatedAt(bundleConfig.getDeploymentConfig().getUri())));
    }


}
