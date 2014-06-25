package at.ac.tuwien.dsg.comot.common.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author omoser
 */
public class ServiceTemplate extends AbstractServiceDescriptionEntity {

    private EntityRelationship relationship;

    ServiceTemplate(String id) {
        super(id);
    }

    private Set<ServiceTopology> serviceNodes = new HashSet<>();

    private Set<EntityRelationship> relationships = new HashSet<>();

    public ServiceTemplate consistsOfTopologies(ServiceTopology... nodes) {
        serviceNodes.addAll(Arrays.asList(nodes));
        return this;
    }

    public ServiceTemplate with(EntityRelationship relationship) {
        relationships.add(relationship);
        return this;
    }

    public ServiceTemplate andRelationships(EntityRelationship... relationships) {
        this.relationships.addAll(Arrays.asList(relationships));
        return this;
    }

    public Set<EntityRelationship> getRelationships() {
        return relationships;
    }
    
    @Override
    public ServiceTemplate provides(ElasticityCapability... capabilities) {
        return (ServiceTemplate) super.provides(capabilities);
    }
    

    @Override
    public ServiceTemplate exposes(Capability... capabilities) {
        return (ServiceTemplate) super.exposes(capabilities);
    }

    @Override
    public ServiceTemplate requires(Requirement... requirements) {
        return (ServiceTemplate) super.requires(requirements);
    }

    @Override
    public ServiceTemplate constrainedBy(Constraint... constraints) {
        return (ServiceTemplate) super.constrainedBy(constraints);
    }

    @Override
    public ServiceTemplate controlledBy(Strategy... strategies) {
        return (ServiceTemplate) super.controlledBy(strategies);
    }

    @Override
    public ServiceTemplate withId(String id) {
        return (ServiceTemplate) super.withId(id);
    }

    @Override
    public ServiceTemplate withDescription(String description) {
        return (ServiceTemplate) super.withDescription(description);
    }

    @Override
    public ServiceTemplate withName(String name) {
        return (ServiceTemplate) super.withName(name);
    }

    @Override
    public ServiceTemplate ofType(String type) {
        return (ServiceTemplate) super.ofType(type);
    }

    @Override
    public String toString() {
        return "ServiceTemplate{"
                + "serviceNodes=" + serviceNodes
                + ", relationships=" + relationships
                + "} " + super.toString();
    }

    public Set<ServiceTopology> getServiceTopologies() {
        return serviceNodes;
    }

    public boolean hasRelationships() {
        return !relationships.isEmpty();
    }

    public static ServiceTemplate ServiceTemplate(String id) {
        return new ServiceTemplate(id);
    }

    public ServiceTemplate withRelationship(final EntityRelationship relationship) {
        this.relationship = relationship;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.serviceNodes);
        hash = 67 * hash + Objects.hashCode(this.relationships);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServiceTemplate other = (ServiceTemplate) obj;
        if (!Objects.equals(this.serviceNodes, other.serviceNodes)) {
            return false;
        }
        return true;
    }

}
