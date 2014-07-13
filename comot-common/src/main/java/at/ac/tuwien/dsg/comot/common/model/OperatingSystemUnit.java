package at.ac.tuwien.dsg.comot.common.model;

/**
 * @author omoser
 */
public class OperatingSystemUnit extends ServiceUnit {

    private OperatingSystemSpecification specification;

    protected OperatingSystemUnit(String id) {
        super(id);
    }

    public static OperatingSystemUnit OperatingSystemUnit(String id) {
        return new OperatingSystemUnit(id).ofType(NodeType.OperatingSystem).andMaxInstances(1);
    }

    public static OperatingSystemUnit UnboundedOperatingSystemUnit(String id) {
        return new OperatingSystemUnit(id).ofType(NodeType.OperatingSystem).andMaxInstances(Integer.MAX_VALUE);
    }

    public OperatingSystemUnit providedBy(OperatingSystemSpecification specification) {
        this.specification = specification;
        return this;
    }

    public OperatingSystemSpecification getSpecification() {
        return specification;
    }

    @Override
    public OperatingSystemUnit withMinInstances(int minInstances) {
        return (OperatingSystemUnit) super.withMinInstances(minInstances);
    }

    @Override
    public OperatingSystemUnit withMaxInstances(int maxInstances) {
        return (OperatingSystemUnit) super.withMaxInstances(maxInstances);
    }

    @Override
    public OperatingSystemUnit andMaxInstances(int maxInstances) {
        return (OperatingSystemUnit) super.andMaxInstances(maxInstances);
    }

    @Override
    public OperatingSystemUnit andMinInstances(int minInstances) {
        return (OperatingSystemUnit) super.andMinInstances(minInstances);
    }

    @Override
    public OperatingSystemUnit deployedBy(ArtifactTemplate... deploymentArtifacts) {
        return (OperatingSystemUnit) super.deployedBy(deploymentArtifacts);
    }

    @Override
    public OperatingSystemUnit withId(String id) {
        return (OperatingSystemUnit) super.withId(id);
    }

    @Override
    public OperatingSystemUnit withDescription(String description) {
        return (OperatingSystemUnit) super.withDescription(description);
    }

    @Override
    public OperatingSystemUnit withName(String name) {
        return (OperatingSystemUnit) super.withName(name);
    }


    @Override
    public OperatingSystemUnit ofType(String type) {
        return (OperatingSystemUnit) super.ofType(type);
    }

    @Override
    public OperatingSystemUnit ofType(NodeType nodeType) {
        return (OperatingSystemUnit) super.ofType(nodeType);
    }

    @Override
    public OperatingSystemUnit controlledBy(Strategy... strategies) {
        return (OperatingSystemUnit) super.controlledBy(strategies);
    }

    @Override
    public OperatingSystemUnit constrainedBy(Constraint... constraints) {
        return (OperatingSystemUnit) super.constrainedBy(constraints);
    }

    @Override
    public OperatingSystemUnit requires(Requirement... requirements) {
        return (OperatingSystemUnit) super.requires(requirements);
    }

    @Override
    public OperatingSystemUnit exposes(Capability... capabilities) {
        return (OperatingSystemUnit) super.exposes(capabilities);
    }
}
