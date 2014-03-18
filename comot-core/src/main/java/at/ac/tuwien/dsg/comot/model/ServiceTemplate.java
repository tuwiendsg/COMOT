package at.ac.tuwien.dsg.comot.model;

/**
 * @author omoser
 */
public class ServiceTemplate extends AbstractServiceDescriptionEntity {

    private ServiceTopology serviceTopology;

    private EntityRelationship relationship;

    ServiceTemplate(String id) {
        super(id);
    }

    public static ServiceTemplate ServiceTemplate(String id) {
        return new ServiceTemplate(id);
    }

    public ServiceTemplate definedBy(final ServiceTopology serviceTopology) {
        this.serviceTopology = serviceTopology;
        return this;
    }

    public ServiceTemplate withRelationship(final EntityRelationship relationship) {
        this.relationship = relationship;
        return this;
    }

    public ServiceTopology getServiceTopology() {
        return serviceTopology;
    }

    @Override
    public ServiceTemplate provides(Capability... capabilities) {
        return (ServiceTemplate) super.provides(capabilities);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceTemplate)) return false;

        ServiceTemplate that = (ServiceTemplate) o;

        if (relationship != null ? !relationship.equals(that.relationship) : that.relationship != null) return false;
        if (serviceTopology != null ? !serviceTopology.equals(that.serviceTopology) : that.serviceTopology != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = serviceTopology != null ? serviceTopology.hashCode() : 0;
        result = 31 * result + (relationship != null ? relationship.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ServiceTemplate{" +
                "serviceTopology=" + serviceTopology +
                ", relationship=" + relationship +
                "} " + super.toString();
    }
}
