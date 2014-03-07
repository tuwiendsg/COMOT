package at.ac.tuwien.dsg.comot.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author omoser
 */
public class ServiceTopology extends AbstractServiceDescriptionEntity {

    private Set<ServiceNode> serviceNodes = new HashSet<>();

    private Set<EntityRelationship> relationships = new HashSet<>();

    public ServiceTopology(String id) {
        super(id);
    }

    public static ServiceTopology ServiceTopology(String id) {
        return new ServiceTopology(id);
    }

    public ServiceTopology consistsOfNodes(ServiceNode... nodes) {
        serviceNodes.addAll(Arrays.asList(nodes));
        return this;
    }

    public ServiceTopology with(EntityRelationship relationship) {
        relationships.add(relationship);
        return this;
    }

    public ServiceTopology andRelationships(EntityRelationship... relationships) {
        this.relationships.addAll(Arrays.asList(relationships));
        return this;
    }

    @Override
    public AbstractServiceDescriptionEntity provides(Capability... capabilities) {
        return super.provides(capabilities);
    }

    @Override
    public AbstractServiceDescriptionEntity requires(Requirement... requirements) {
        return super.requires(requirements);
    }

    @Override
    public AbstractServiceDescriptionEntity constrainedBy(Constraint... constraints) {
        return super.constrainedBy(constraints);
    }

    @Override
    public AbstractServiceDescriptionEntity controlledBy(Strategy... strategies) {
        return super.controlledBy(strategies);
    }

    @Override
    public AbstractCloudEntity withId(String id) {
        return super.withId(id);
    }

    @Override
    public AbstractCloudEntity withDescription(String description) {
        return super.withDescription(description);
    }

    @Override
    public AbstractCloudEntity withName(String name) {
        return super.withName(name);
    }

    @Override
    public AbstractCloudEntity withType(String type) {
        return super.withType(type);
    }

    @Override
    public AbstractCloudEntity ofType(String type) {
        return super.ofType(type);
    }

    @Override
    public String toString() {
        return "ServiceTopology{" +
                "serviceNodes=" + serviceNodes +
                ", relationships=" + relationships +
                "} " + super.toString();
    }
}
