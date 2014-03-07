package at.ac.tuwien.dsg.comot.model;

/**
 * @author omoser
 */
public class ServiceNode extends AbstractServiceDescriptionEntity {

    private int minInstances;

    private int maxInstances;

    public ServiceNode(String id) {
        super(id);
    }

    public static ServiceNode ServiceNode(String id) {
        return new ServiceNode(id);
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
    public ServiceNode withType(String type) {
        return (ServiceNode) super.withType(type);
    }

    @Override
    public ServiceNode ofType(String type) {
        return (ServiceNode) super.ofType(type);
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
