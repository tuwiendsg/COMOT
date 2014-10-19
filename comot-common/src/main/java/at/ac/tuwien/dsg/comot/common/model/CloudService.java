package at.ac.tuwien.dsg.comot.common.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author omoser
 */
public class CloudService extends AbstractServiceDescriptionEntity {

    private EntityRelationship relationship;

    //TODO: implement this in API
    private String metricCompositonRulesFile;

    private String effectsCompositonRulesFile;

    CloudService(String id) {
        super(id);
    }

    private Set<ServiceTopology> serviceNodes = new HashSet<>();

    private Set<EntityRelationship> relationships = new HashSet<>();

    public CloudService consistsOfTopologies(ServiceTopology... nodes) {
        serviceNodes.addAll(Arrays.asList(nodes));
        return this;
    }

    public CloudService with(EntityRelationship relationship) {
        relationships.add(relationship);
        return this;
    }

    public CloudService andRelationships(EntityRelationship... relationships) {
        this.relationships.addAll(Arrays.asList(relationships));
        return this;
    }

    public Set<EntityRelationship> getRelationships() {
        return relationships;
    }

    public static CloudService CloudService(String id) {
        return new CloudService(id);
    }

    @Override

    public CloudService provides(ElasticityCapability... capabilities) {
        return (CloudService) super.provides(capabilities);
    }

    @Override
    public CloudService exposes(Capability... capabilities) {
        return (CloudService) super.exposes(capabilities);
    }

    @Override
    public CloudService requires(Requirement... requirements) {
        return (CloudService) super.requires(requirements);
    }

    @Override
    public CloudService constrainedBy(Constraint... constraints) {
        return (CloudService) super.constrainedBy(constraints);
    }

    @Override
    public CloudService controlledBy(Strategy... strategies) {
        return (CloudService) super.controlledBy(strategies);
    }

    @Override
    public CloudService withId(String id) {
        return (CloudService) super.withId(id);
    }

    @Override
    public CloudService withDescription(String description) {
        return (CloudService) super.withDescription(description);
    }

    @Override
    public CloudService withName(String name) {
        return (CloudService) super.withName(name);
    }

    @Override
    public CloudService ofType(String type) {
        return (CloudService) super.ofType(type);
    }

    public boolean hasRelationships() {
        return !relationships.isEmpty();
    }

    public CloudService withMetricCompositonRulesFile(final String metricCompositonRulesFile) {
        this.metricCompositonRulesFile = metricCompositonRulesFile;
        return this;
    }

    public CloudService withDefaultMetrics() {
        this.metricCompositonRulesFile = "./elasticity/compositionRules.xml";
        return this;
    }

    public String getMetricCompositonRulesFile() {
        return metricCompositonRulesFile;
    }

    public CloudService withActionEffectsCompositonRulesFile(final String effectsCompositonRulesFile) {
        this.effectsCompositonRulesFile = effectsCompositonRulesFile;
        return this;
    }

    public CloudService withDefaultActionEffects() {
        this.effectsCompositonRulesFile = "./elasticity/effects.json";
        return this;
    }

    public String getEffectsCompositonRulesFile() {
        return effectsCompositonRulesFile;
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

    public static CloudService ServiceTemplate(String id) {
        return new CloudService(id).withName(id);
    }

    public CloudService withRelationship(final EntityRelationship relationship) {
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
        final CloudService other = (CloudService) obj;
        if (!Objects.equals(this.serviceNodes, other.serviceNodes)) {
            return false;
        }
        return true;
    }

}
