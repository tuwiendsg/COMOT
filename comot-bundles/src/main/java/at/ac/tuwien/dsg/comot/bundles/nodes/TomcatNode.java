package at.ac.tuwien.dsg.comot.bundles.nodes;

import at.ac.tuwien.dsg.comot.bundles.JsonBundleLoader;
import at.ac.tuwien.dsg.comot.common.model.BundleConfig;
import at.ac.tuwien.dsg.comot.common.model.SoftwareNode;

import static at.ac.tuwien.dsg.comot.common.model.ArtifactReference.ArtifactReference;
import static at.ac.tuwien.dsg.comot.common.model.ArtifactTemplate.ScriptArtifactTemplate;

/**
 * @author omoser
 */
public class TomcatNode extends SoftwareNode {

    protected TomcatNode(String id) {
        super(id);
    }

    public static TomcatNode Tomcat7Node(String id) {
        BundleConfig bundleConfig = JsonBundleLoader.getInstance().getBundleConfig("tomcat");

        return (TomcatNode) new TomcatNode(id)
                .deployedBy(ScriptArtifactTemplate("id")
                        .withBundleConfig(bundleConfig)
                        .consistsOf(ArtifactReference("tomcat").locatedAt(bundleConfig.getDeploymentConfig().getUri())));
    }
}
