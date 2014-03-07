package at.ac.tuwien.dsg.comot.model;

import java.util.*;

/**
 * @author omoser
 */
public class CloudApplication extends AbstractServiceDescriptionEntity {

    private Set<ServiceTemplate> serviceTemplates = new HashSet<>();

    CloudApplication(String id) {
        super(id);
        context.put(id, this);
    }

    public static CloudApplication CloudApplication(String id) {
        return new CloudApplication(id);
    }


    public CloudApplication consistsOfServices(ServiceTemplate... serviceTemplates) {
        this.serviceTemplates.addAll(Arrays.asList(serviceTemplates));
        return this;
    }

    public Set<ServiceTemplate> getServiceTemplates() {
        return serviceTemplates;
    }

    @Override
    public CloudApplication provides(Capability... capabilities) {
        return (CloudApplication) super.provides(capabilities);
    }

    @Override
    public CloudApplication requires(Requirement... requirements) {
        return (CloudApplication) super.requires(requirements);
    }

    @Override
    public CloudApplication constrainedBy(Constraint... constraints) {
        return (CloudApplication) super.constrainedBy(constraints);
    }

    @Override
    public CloudApplication controlledBy(Strategy... strategies) {
        return (CloudApplication) super.controlledBy(strategies);
    }

    @Override
    public CloudApplication withId(String id) {
        return (CloudApplication) super.withId(id);
    }

    @Override
    public CloudApplication withDescription(String description) {
        return (CloudApplication) super.withDescription(description);
    }

    @Override
    public CloudApplication withName(String name) {
        return (CloudApplication) super.withName(name);
    }

    @Override
    public CloudApplication withType(String type) {
        return (CloudApplication) super.withType(type);
    }

    @Override
    public CloudApplication ofType(String type) {
        return (CloudApplication) super.ofType(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CloudApplication)) return false;
        if (!super.equals(o)) return false;

        CloudApplication that = (CloudApplication) o;

        if (serviceTemplates != null ? !serviceTemplates.equals(that.serviceTemplates) : that.serviceTemplates != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (serviceTemplates != null ? serviceTemplates.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CloudApplication{" +
                "serviceTemplates=" + serviceTemplates +
                "} " + super.toString();
    }


}
