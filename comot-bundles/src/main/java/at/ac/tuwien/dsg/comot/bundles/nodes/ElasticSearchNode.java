package at.ac.tuwien.dsg.comot.bundles.nodes;

import at.ac.tuwien.dsg.comot.bundles.BundleLoader;
import at.ac.tuwien.dsg.comot.bundles.JsonBundleLoader;
import at.ac.tuwien.dsg.comot.common.model.ArtifactReference;
import at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate;
import at.ac.tuwien.dsg.comot.common.model.BundleConfig;
import at.ac.tuwien.dsg.comot.common.model.ServiceNode;

/**
 * @author omoser
 */
public class ElasticSearchNode extends ServiceNode {

    protected ElasticSearchNode(String id) {
        super(id);
    }

    public static ElasticSearchNode ElasticSearchNode(String id) {
        BundleLoader bundleLoader = JsonBundleLoader.getInstance();
        BundleConfig bundleConfig = bundleLoader.getBundleConfig("elasticsearch");

        return (ElasticSearchNode) new ElasticSearchNode(id)
                .ofType(NodeType.Software)
                .deployedBy(ArtifactTemplate.ArtifactTemplate("id")
                        .ofType(ArtifactTemplate.ArtifactType.Script)
                        .withBundleConfig(bundleConfig)
                        .consistsOf(
                                ArtifactReference.ArtifactReference("elasticseach")
                                        .locatedAt(bundleConfig.getDeploymentConfig().getUri())
                        ));
    }



}
