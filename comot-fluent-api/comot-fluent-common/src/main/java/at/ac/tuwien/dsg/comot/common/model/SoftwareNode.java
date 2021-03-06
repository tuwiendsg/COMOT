package at.ac.tuwien.dsg.comot.common.model;

/**
 * @author omoser
 */
public class SoftwareNode extends ServiceUnit {

    protected SoftwareNode(String id) {
        super(id);
        ofType(NodeType.Software);
    }

    public static SoftwareNode SoftwareNode(String id) {
        return new SoftwareNode(id).ofType(NodeType.Software);
    }

    public static SoftwareNode SingleSoftwareUnit(String id) {
        return new SoftwareNode(id).ofType(NodeType.Software).withMinInstances(1).withMaxColocatedInstances(1);
    }

    public static SoftwareNode UnboundedSoftwareUnit(String id) {
        return new SoftwareNode(id).ofType(NodeType.Software).withMinInstances(1).withMaxColocatedInstances(Integer.MAX_VALUE)
                .provides(ElasticityCapability.ScaleIn())
                .provides(ElasticityCapability.ScaleOut());
    }
    
    public static SoftwareNode WarNode(String id){
        return new SoftwareNode(id).ofType(NodeType.War);
    }
    
    public static SoftwareNode SingleWarUnit(String id) {
        return new SoftwareNode(id).ofType(NodeType.War).withMinInstances(1).withMaxColocatedInstances(1);
    }

    @Override
    public SoftwareNode withMinInstances(int minInstances) {
        return (SoftwareNode) super.withMinInstances(minInstances);
    }

    @Override
    public SoftwareNode withMaxColocatedInstances(int maxInstances) {
        return (SoftwareNode) super.withMaxColocatedInstances(maxInstances);
    }

    @Override
    public SoftwareNode andMaxInstances(int maxInstances) {
        return (SoftwareNode) super.andMaxInstances(maxInstances);
    }

    @Override
    public SoftwareNode andMinInstances(int minInstances) {
        return (SoftwareNode) super.andMinInstances(minInstances);
    }

    @Override
    public SoftwareNode deployedBy(ArtifactTemplate... deploymentArtifacts) {
        return (SoftwareNode) super.deployedBy(deploymentArtifacts);
    }

    @Override
    public SoftwareNode withId(String id) {
        return (SoftwareNode) super.withId(id);
    }

    @Override
    public SoftwareNode withDescription(String description) {
        return (SoftwareNode) super.withDescription(description);
    }

    @Override
    public SoftwareNode withName(String name) {
        return (SoftwareNode) super.withName(name);
    }

    @Override
    public SoftwareNode ofType(String type) {
        return (SoftwareNode) super.ofType(type);
    }

    @Override
    public SoftwareNode ofType(NodeType nodeType) {
        return (SoftwareNode) super.ofType(nodeType);
    }

    @Override
    public SoftwareNode controlledBy(Strategy... strategies) {
        return (SoftwareNode) super.controlledBy(strategies);
    }

    @Override
    public SoftwareNode constrainedBy(Constraint... constraints) {
        return (SoftwareNode) super.constrainedBy(constraints);
    }

    @Override
    public SoftwareNode requires(Requirement... requirements) {
        return (SoftwareNode) super.requires(requirements);
    }

    @Override
    public SoftwareNode exposes(Capability... capabilities) {
        return (SoftwareNode) super.exposes(capabilities);
    }

    @Override
    public SoftwareNode provides(ElasticityCapability... capabilities) {
        return (SoftwareNode) super.provides(capabilities);
    }

    @Override
    public SoftwareNode withLifecycleAction(LifecyclePhase phase, AbstractLifecycleAction action) {
        lifecycleActions.put(phase, action);
        return this;
    }

    @Override
    public SoftwareNode removeLifecycleAction(LifecyclePhase phase, AbstractLifecycleAction action) {
        lifecycleActions.remove(phase);
        return this;
    }

}
