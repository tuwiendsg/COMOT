package at.ac.tuwien.dsg.comot.common.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author omoser
 */
public class ServiceTopology extends AbstractServiceDescriptionEntity {

    private Set<ServiceUnit> serviceUnits = new HashSet<>();

    private Set<EntityRelationship> relationships = new HashSet<>();

    ServiceTopology(String id) {
        super(id);
    }

    public static ServiceTopology ServiceTopology(String id) {
        return new ServiceTopology(id);
    }

    public ServiceTopology consistsOf(ServiceUnit... nodes) {
        serviceUnits.addAll(Arrays.asList(nodes));
        return this;
    }

    public ServiceTopology with(EntityRelationship relationship) {
        relationships.add(relationship);
        return this;
    }

    public ServiceTopology with(EntityRelationship... relationships) {
        this.relationships.addAll(Arrays.asList(relationships));
        return this;
    }

    public Set<EntityRelationship> getRelationships() {
        return relationships;
    }

    @Override
    public AbstractServiceDescriptionEntity exposes(Capability... capabilities) {
        return super.exposes(capabilities);
    }

    public Set<ServiceUnit> getServiceUnits() {
        return serviceUnits;
    }

}
