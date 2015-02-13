package at.ac.tuwien.dsg.comot.common.model;

/**
 * @author omoser
 */
public class DockerUnit extends ServiceUnit {

    private OperatingSystemSpecification specification;

    protected DockerUnit(String id) {
        super(id);
        super.andMaxInstances(Integer.MAX_VALUE);
    }

    public static DockerUnit DockerUnit(String id) {
        return new DockerUnit(id).ofType(NodeType.Docker);
    }

    public DockerUnit providedBy(OperatingSystemSpecification specification) {
        this.specification = specification;
        return this;
    }

    public OperatingSystemSpecification getSpecification() {
        return specification;
    }

    @Override
    public DockerUnit withMinInstances(int minInstances) {
        return (DockerUnit) super.withMinInstances(minInstances);
    }

    @Override
    public DockerUnit withMaxColocatedInstances(int maxInstances) {
        return (DockerUnit) super.withMaxColocatedInstances(maxInstances);
    }

    @Override
    public DockerUnit andMaxInstances(int maxInstances) {
        return (DockerUnit) super.andMaxInstances(maxInstances);
    }

    @Override
    public DockerUnit andMinInstances(int minInstances) {
        return (DockerUnit) super.andMinInstances(minInstances);
    }

    @Override
    public DockerUnit deployedBy(ArtifactTemplate... deploymentArtifacts) {
        return (DockerUnit) super.deployedBy(deploymentArtifacts);
    }

    @Override
    public DockerUnit withId(String id) {
        return (DockerUnit) super.withId(id);
    }

    @Override
    public DockerUnit withDescription(String description) {
        return (DockerUnit) super.withDescription(description);
    }

    @Override
    public DockerUnit withName(String name) {
        return (DockerUnit) super.withName(name);
    }

    @Override
    public DockerUnit ofType(String type) {
        return (DockerUnit) super.ofType(type);
    }

    @Override
    public DockerUnit ofType(NodeType nodeType) {
        return (DockerUnit) super.ofType(nodeType);
    }

    @Override
    public DockerUnit controlledBy(Strategy... strategies) {
        return (DockerUnit) super.controlledBy(strategies);
    }

    @Override
    public DockerUnit constrainedBy(Constraint... constraints) {
        return (DockerUnit) super.constrainedBy(constraints);
    }

    @Override
    public DockerUnit requires(Requirement... requirements) {
        return (DockerUnit) super.requires(requirements);
    }

    @Override
    public DockerUnit exposes(Capability... capabilities) {
        return (DockerUnit) super.exposes(capabilities);
    }

    @Override
    public DockerUnit withLifecycleAction(LifecyclePhase phase, AbstractLifecycleAction action) {
        lifecycleActions.put(phase, action);
        return this;
    }

    @Override
    public DockerUnit removeLifecycleAction(LifecyclePhase phase, AbstractLifecycleAction action) {
        lifecycleActions.remove(phase);
        return this;
    }
}
