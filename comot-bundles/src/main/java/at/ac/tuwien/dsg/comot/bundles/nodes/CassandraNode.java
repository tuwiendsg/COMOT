package at.ac.tuwien.dsg.comot.bundles.nodes;

import at.ac.tuwien.dsg.comot.bundles.JsonBundleLoader;
import at.ac.tuwien.dsg.comot.common.model.BundleConfig;
import at.ac.tuwien.dsg.comot.common.model.SoftwareNode;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactReference.ArtifactReference;
import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.ScriptArtifactTemplate;

/**
 * @author omoser
 */
public class CassandraNode extends SoftwareNode {

    protected CassandraNode(String id) {
        super(id);
    }

    public static CassandraNode CassandraNode(String id) {
        BundleConfig bundleConfig = JsonBundleLoader.getInstance().getBundleConfig("cassandra");

        return (CassandraNode) new CassandraNode(id)
                .deployedBy(ScriptArtifactTemplate("id")
                        .withBundleConfig(bundleConfig)
                        .consistsOf(ArtifactReference("cassandra").locatedAt(bundleConfig.getDeploymentConfig().getUri())));
    }
}
