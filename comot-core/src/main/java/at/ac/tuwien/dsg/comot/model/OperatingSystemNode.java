package at.ac.tuwien.dsg.comot.model;

/**
 * @author omoser
 */
public class OperatingSystemNode extends ServiceNode {

    private OperatingSystemSpecification specification;

    OperatingSystemNode(String id) {
        super(id);
    }

    public static OperatingSystemNode OperatingSystemNode(String id) {
        return new OperatingSystemNode(id).ofType(NodeType.OperatingSystem);
    }

    public OperatingSystemNode specifiedBy(OperatingSystemSpecification specification) {
        this.specification = specification;
        return this;
    }

    public OperatingSystemSpecification getSpecification() {
        return specification;
    }

    @Override
    public OperatingSystemNode withMinInstances(int minInstances) {
        return (OperatingSystemNode) super.withMinInstances(minInstances);
    }

    @Override
    public OperatingSystemNode withMaxInstances(int maxInstances) {
        return (OperatingSystemNode) super.withMaxInstances(maxInstances);
    }

    @Override
    public OperatingSystemNode andMaxInstances(int maxInstances) {
        return (OperatingSystemNode) super.andMaxInstances(maxInstances);
    }

    @Override
    public OperatingSystemNode andMinInstances(int minInstances) {
        return (OperatingSystemNode) super.andMinInstances(minInstances);
    }

    @Override
    public OperatingSystemNode deployedBy(ArtifactTemplate... deploymentArtifacts) {
        return (OperatingSystemNode) super.deployedBy(deploymentArtifacts);
    }

    @Override
    public OperatingSystemNode withId(String id) {
        return (OperatingSystemNode) super.withId(id);
    }

    @Override
    public OperatingSystemNode withDescription(String description) {
        return (OperatingSystemNode) super.withDescription(description);
    }

    @Override
    public OperatingSystemNode withName(String name) {
        return (OperatingSystemNode) super.withName(name);
    }

    @Override
    public OperatingSystemNode withType(String type) {
        return (OperatingSystemNode) super.withType(type);
    }

    @Override
    public OperatingSystemNode ofType(String type) {
        return (OperatingSystemNode) super.ofType(type);
    }

    @Override
    public OperatingSystemNode ofType(NodeType nodeType) {
        return (OperatingSystemNode) super.ofType(nodeType);
    }

    @Override
    public OperatingSystemNode controlledBy(Strategy... strategies) {
        return (OperatingSystemNode) super.controlledBy(strategies);
    }

    @Override
    public OperatingSystemNode constrainedBy(Constraint... constraints) {
        return (OperatingSystemNode) super.constrainedBy(constraints);
    }

    @Override
    public OperatingSystemNode requires(Requirement... requirements) {
        return (OperatingSystemNode) super.requires(requirements);
    }

    @Override
    public OperatingSystemNode provides(Capability... capabilities) {
        return (OperatingSystemNode) super.provides(capabilities);
    }
}
