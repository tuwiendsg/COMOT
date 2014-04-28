package at.ac.tuwien.dsg.comot.bundles.dataends;

import at.ac.tuwien.dsg.comot.bundles.BundleLoaderFactory;
import at.ac.tuwien.dsg.comot.common.model.BundleConfig;
import at.ac.tuwien.dsg.comot.common.model.SoftwareNode;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactReference.ArtifactReference;
import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.ScriptArtifactTemplate;

/**
 * @author omoser
 */
public class PostgresNode extends SoftwareNode {

    protected PostgresNode(String id) {
        super(id);
    }

    public static PostgresNode PostgresNode(String id) {
        BundleConfig bundleConfig = BundleLoaderFactory.getDefaultBundleLoader().getBundleConfig("postgres");
        return (PostgresNode) new PostgresNode(id)
                .deployedBy(ScriptArtifactTemplate("postgres")
                        .withBundleConfig(bundleConfig)
                        .consistsOf(ArtifactReference("postgres").locatedAt(bundleConfig.getDeploymentConfig().getUri())));
    }
}
