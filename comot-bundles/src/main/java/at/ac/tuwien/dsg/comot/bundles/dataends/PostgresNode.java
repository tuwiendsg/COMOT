package at.ac.tuwien.dsg.comot.bundles.dataends;

import at.ac.tuwien.dsg.comot.bundles.BundleLoaderFactory;
import at.ac.tuwien.dsg.comot.common.fluent.BundleConfig;
import at.ac.tuwien.dsg.comot.common.fluent.SoftwareNode;
import static at.ac.tuwien.dsg.comot.common.fluent.ArtifactReference.ArtifactReference;
import static at.ac.tuwien.dsg.comot.common.fluent.ArtifactTemplate.ScriptArtifactTemplate;

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
