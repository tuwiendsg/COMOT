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
public class CassandraNode extends ServiceNode {

    protected CassandraNode(String id) {
        super(id);
    }

    public static CassandraNode CassandraNode(String id) {
        BundleLoader bundleLoader = JsonBundleLoader.getInstance();
        BundleConfig bundleConfig = bundleLoader.getBundleConfig("cassandra");

        return (CassandraNode) new ElasticSearchNode(id)
                .ofType(NodeType.Software)
                .deployedBy(ArtifactTemplate.ArtifactTemplate("id")
                        .ofType(ArtifactTemplate.ArtifactType.Script)
                        .withBundleConfig(bundleConfig)
                        .consistsOf(
                                ArtifactReference.ArtifactReference("cassandra")
                                        .locatedAt(bundleConfig.getDeploymentConfig().getUri())
                        ));
    }
}
