package at.ac.tuwien.dsg.comot.bundles.dataends;

import at.ac.tuwien.dsg.comot.bundles.BundleLoaderFactory;
import at.ac.tuwien.dsg.comot.common.fluent.*;
import static at.ac.tuwien.dsg.comot.common.fluent.ArtifactReference.ArtifactReference;
import static at.ac.tuwien.dsg.comot.common.fluent.ArtifactTemplate.ScriptArtifactTemplate;

/**
 * @author omoser
 */
public class CassandraNode extends SoftwareNode {

    protected CassandraNode(String id) {
        super(id);
    }

    public static CassandraNode CassandraNode(String id) {
        BundleConfig bundleConfig = BundleLoaderFactory.getDefaultBundleLoader().getBundleConfig("cassandra");
        return new CassandraNode(id)
                .deployedBy(ScriptArtifactTemplate("id")
                        .withBundleConfig(bundleConfig)
                        .consistsOf(ArtifactReference("cassandra").locatedAt(bundleConfig.getDeploymentConfig().getUri())));
    }

    @Override
    public CassandraNode withMinInstances(int minInstances) {
        return (CassandraNode) super.withMinInstances(minInstances);
    }

    @Override
    public CassandraNode withMaxInstances(int maxInstances) {
        return (CassandraNode) super.withMaxInstances(maxInstances);
    }

    @Override
    public CassandraNode andMaxInstances(int maxInstances) {
        return (CassandraNode) super.andMaxInstances(maxInstances);
    }

    @Override
    public CassandraNode andMinInstances(int minInstances) {
        return (CassandraNode) super.andMinInstances(minInstances);
    }

    @Override
    public CassandraNode deployedBy(ArtifactTemplate... deploymentArtifacts) {
        return (CassandraNode) super.deployedBy(deploymentArtifacts);
    }

    @Override
    public CassandraNode withId(String id) {
        return (CassandraNode) super.withId(id);
    }

    @Override
    public CassandraNode withDescription(String description) {
        return (CassandraNode) super.withDescription(description);
    }

    @Override
    public CassandraNode withName(String name) {
        return (CassandraNode) super.withName(name);
    }

    @Override
    public CassandraNode ofType(String type) {
        return (CassandraNode) super.ofType(type);
    }

    @Override
    public CassandraNode ofType(NodeType nodeType) {
        return (CassandraNode) super.ofType(nodeType);
    }

    @Override
    public CassandraNode controlledBy(Strategy... strategies) {
        return (CassandraNode) super.controlledBy(strategies);
    }

    @Override
    public CassandraNode constrainedBy(Constraint... constraints) {
        return (CassandraNode) super.constrainedBy(constraints);
    }

    @Override
    public CassandraNode requires(Requirement... requirements) {
        return (CassandraNode) super.requires(requirements);
    }

    @Override
    public CassandraNode exposes(Capability... capabilities) {
        return (CassandraNode) super.exposes(capabilities);
    }
}
