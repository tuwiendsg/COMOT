package at.ac.tuwien.dsg.comot.common.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author omoser
 */
public class ServiceUnit extends AbstractServiceDescriptionEntity {

    public static final int DEFAULT_INSTANCES = 1;

    private int minInstances = DEFAULT_INSTANCES;

    private int maxInstances = DEFAULT_INSTANCES;

    private Set<ArtifactTemplate> deploymentArtifacts = new HashSet<>();

    public enum NodeType {

        OperatingSystem("os"), 
        Docker("docker"),
        War("war"),
        Software("software");

        final String type;

        NodeType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    protected ServiceUnit(String id) {
        super(id);
    }

    public static ServiceUnit ServiceNode(String id) {
        return new ServiceUnit(id);
    }

    public Set<ArtifactTemplate> getDeploymentArtifacts() {
        return deploymentArtifacts;
    }

    public int getMinInstances() {
        return minInstances;
    }

    public int getMaxInstances() {
        return maxInstances;
    }

    public ServiceUnit withMinInstances(final int minInstances) {
        this.minInstances = minInstances;
        return this;
    }

    public ServiceUnit withMaxColocatedInstances(final int maxInstances) {
        this.maxInstances = maxInstances;
        return this;
    }

    public ServiceUnit andMaxInstances(final int maxInstances) {
        return withMaxColocatedInstances(maxInstances);
    }

    public ServiceUnit andMinInstances(final int minInstances) {
        return withMinInstances(minInstances);
    }

    public ServiceUnit deployedBy(ArtifactTemplate... deploymentArtifacts) {
        this.deploymentArtifacts.addAll(Arrays.asList(deploymentArtifacts));
        return this;
    }

    @Override
    public ServiceUnit withId(String id) {
        return (ServiceUnit) super.withId(id);
    }

    @Override
    public ServiceUnit withDescription(String description) {
        return (ServiceUnit) super.withDescription(description);
    }

    @Override
    public ServiceUnit withName(String name) {
        return (ServiceUnit) super.withName(name);
    }

    @Override
    public ServiceUnit ofType(String type) {
        return (ServiceUnit) super.ofType(type);
    }

    public ServiceUnit ofType(NodeType nodeType) {
        return (ServiceUnit) super.ofType(nodeType.toString());
    }

    @Override
    public ServiceUnit controlledBy(Strategy... strategies) {
        return (ServiceUnit) super.controlledBy(strategies);
    }

    @Override
    public ServiceUnit constrainedBy(Constraint... constraints) {
        return (ServiceUnit) super.constrainedBy(constraints);
    }

    @Override
    public ServiceUnit requires(Requirement... requirements) {
        return (ServiceUnit) super.requires(requirements);
    }

    @Override
    public ServiceUnit exposes(Capability... capabilities) {
        return (ServiceUnit) super.exposes(capabilities);
    }

    @Override
    public ServiceUnit provides(ElasticityCapability... capabilities) {
        return (ServiceUnit) super.provides(capabilities);
    }
    
    
    @Override
    public ServiceUnit withLifecycleAction(LifecyclePhase phase, AbstractLifecycleAction action) {
        lifecycleActions.put(phase, action);
        return this;
    }

    @Override
    public ServiceUnit removeLifecycleAction(LifecyclePhase phase, AbstractLifecycleAction action) {
        lifecycleActions.remove(phase);
        return this;
    }

    @Override
    public String toString() {
        return "ServiceNode{"
                + "minInstances=" + minInstances
                + ", maxInstances=" + maxInstances
                + "} " + super.toString();
    }
}
