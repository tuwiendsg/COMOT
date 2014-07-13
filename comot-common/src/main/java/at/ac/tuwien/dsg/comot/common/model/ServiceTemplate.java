package at.ac.tuwien.dsg.comot.common.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author omoser
 */
public class ServiceTemplate extends AbstractServiceDescriptionEntity {

    //TODO: implement this in API
    private String metricCompositonRulesFile;

    private String effectsCompositonRulesFile;

    ServiceTemplate(String id) {
        super(id);
    }

    public static ServiceTemplate ServiceTemplate(String id) {
        return new ServiceTemplate(id);
    }

    private Set<ServiceTopology> serviceTopologies = new HashSet<>();

    private Set<EntityRelationship> relationships = new HashSet<>();

    public ServiceTemplate consistsOf(ServiceTopology... nodes) {
        serviceTopologies.addAll(Arrays.asList(nodes));
        return this;
    }

    public ServiceTemplate with(EntityRelationship... relationships) {
        this.relationships.addAll(Arrays.asList(relationships));
        return this;
    }

    public Set<EntityRelationship> getRelationships() {
        return relationships;
    }

    public Set<ServiceTopology> getServiceTopologies() {
        return serviceTopologies;
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


    public boolean hasRelationships() {
        return !relationships.isEmpty();
    }

    public ServiceTemplate withMetricCompositonRulesFile(final String metricCompositonRulesFile) {
        this.metricCompositonRulesFile = metricCompositonRulesFile;
        return this;
    }


    public ServiceTemplate withDefaultMetrics() {
        this.metricCompositonRulesFile = "./config/resources/compositionRules.xml";
        return this;
    }

    public String getMetricCompositonRulesFile() {
        return metricCompositonRulesFile;
    }


    public ServiceTemplate withActionEffectsCompositonRulesFile(final String effectsCompositonRulesFile) {
        this.effectsCompositonRulesFile = effectsCompositonRulesFile;
        return this;
    }


    public ServiceTemplate withDefaultActionEffects() {
        this.effectsCompositonRulesFile = "./config/resources/effects.json";
        return this;
    }

    public String getEffectsCompositonRulesFile() {
        return effectsCompositonRulesFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceTemplate)) return false;
        if (!super.equals(o)) return false;

        ServiceTemplate that = (ServiceTemplate) o;

        if (effectsCompositonRulesFile != null ? !effectsCompositonRulesFile.equals(that.effectsCompositonRulesFile) : that.effectsCompositonRulesFile != null)
            return false;
        if (metricCompositonRulesFile != null ? !metricCompositonRulesFile.equals(that.metricCompositonRulesFile) : that.metricCompositonRulesFile != null)
            return false;
        if (relationships != null ? !relationships.equals(that.relationships) : that.relationships != null)
            return false;
        if (serviceTopologies != null ? !serviceTopologies.equals(that.serviceTopologies) : that.serviceTopologies != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (metricCompositonRulesFile != null ? metricCompositonRulesFile.hashCode() : 0);
        result = 31 * result + (effectsCompositonRulesFile != null ? effectsCompositonRulesFile.hashCode() : 0);
        result = 31 * result + (serviceTopologies != null ? serviceTopologies.hashCode() : 0);
        result = 31 * result + (relationships != null ? relationships.hashCode() : 0);
        return result;
    }


}
