package at.ac.tuwien.dsg.comot.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author omoser
 */
public class ServiceNode extends AbstractServiceDescriptionEntity {

    public static final int DEFAULT_INSTANCES = 1;

    private int minInstances = DEFAULT_INSTANCES;

    private int maxInstances = DEFAULT_INSTANCES;

    private Set<ArtifactTemplate> deploymentArtifacts = new HashSet<>();

    public enum NodeType {

        OperatingSystem("os"), Software("software");

        final String type;

        NodeType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }


    ServiceNode(String id) {
        super(id);
    }

    public static ServiceNode ServiceNode(String id) {
        return new ServiceNode(id);
    }

    public static ServiceNode SoftwareNode(String id) {
        return new ServiceNode(id).ofType(NodeType.Software);
    }

    public static ServiceNode SingleSoftwareNode(String id) {
        return new ServiceNode(id).ofType(NodeType.Software).withMinInstances(1).withMaxInstances(1);
    }

    public static ServiceNode UnboundedSoftwareNode(String id) {
        return new ServiceNode(id).ofType(NodeType.Software).withMinInstances(1).withMaxInstances(Integer.MAX_VALUE);
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

    public ServiceNode withMinInstances(final int minInstances) {
        this.minInstances = minInstances;
        return this;
    }

    public ServiceNode withMaxInstances(final int maxInstances) {
        this.maxInstances = maxInstances;
        return this;
    }

    public ServiceNode andMaxInstances(final int maxInstances) {
        return withMaxInstances(maxInstances);
    }

    public ServiceNode andMinInstances(final int minInstances) {
        return withMinInstances(minInstances);
    }

    public ServiceNode deployedBy(ArtifactTemplate... deploymentArtifacts) {
        this.deploymentArtifacts.addAll(Arrays.asList(deploymentArtifacts));
        return this;
    }


    @Override
    public ServiceNode withId(String id) {
        return (ServiceNode) super.withId(id);
    }

    @Override
    public ServiceNode withDescription(String description) {
        return (ServiceNode) super.withDescription(description);
    }

    @Override
    public ServiceNode withName(String name) {
        return (ServiceNode) super.withName(name);
    }

    @Override
    public ServiceNode ofType(String type) {
        return (ServiceNode) super.ofType(type);
    }

    public ServiceNode ofType(NodeType nodeType) {
        return (ServiceNode) super.ofType(nodeType.toString());
    }

    @Override
    public ServiceNode controlledBy(Strategy... strategies) {
        return (ServiceNode) super.controlledBy(strategies);
    }

    @Override
    public ServiceNode constrainedBy(Constraint... constraints) {
        return (ServiceNode) super.constrainedBy(constraints);
    }

    @Override
    public ServiceNode requires(Requirement... requirements) {
        return (ServiceNode) super.requires(requirements);
    }

    @Override
    public ServiceNode provides(Capability... capabilities) {
        return (ServiceNode) super.provides(capabilities);
    }

    @Override
    public String toString() {
        return "ServiceNode{" +
                "minInstances=" + minInstances +
                ", maxInstances=" + maxInstances +
                "} " + super.toString();
    }
}
