package at.ac.tuwien.dsg.comot.common.model;
//
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * @author omoser
// */
//public class CloudService extends AbstractServiceDescriptionEntity {
//
//    private Set<ServiceTemplate> serviceTemplates = new HashSet<>();
//
//    protected boolean defaultMetricsEnabled;
//
//    protected CloudService(String id) {
//        super(id);
//    }
//
//    public static CloudService CloudService(String id) {
//        return new CloudService(id);
//    }
//
//
//    public CloudService consistsOfServices(ServiceTemplate... serviceTemplates) {
//        this.serviceTemplates.addAll(Arrays.asList(serviceTemplates));
//        return this;
//    }
//
//    public Set<ServiceTemplate> getServiceTemplates() {
//        return serviceTemplates;
//    }
//
//    @Override
//    public CloudService exposes(Capability... capabilities) {
//        return (CloudService) super.exposes(capabilities);
//    }
//
//    @Override
//    public CloudService requires(Requirement... requirements) {
//        return (CloudService) super.requires(requirements);
//    }
//
//    @Override
//    public CloudService constrainedBy(Constraint... constraints) {
//        return (CloudService) super.constrainedBy(constraints);
//    }
//
//    @Override
//    public CloudService controlledBy(Strategy... strategies) {
//        return (CloudService) super.controlledBy(strategies);
//    }
//
//    @Override
//    public CloudService withId(String id) {
//        return (CloudService) super.withId(id);
//    }
//
//    public CloudService withDefaultMetricsEnabled(final boolean defaultMetricsEnabled) {
//        this.defaultMetricsEnabled = defaultMetricsEnabled;
//        return this;
//    }
//
//    public void setDefaultMetricsEnabled(boolean defaultMetricsEnabled) {
//        this.defaultMetricsEnabled = defaultMetricsEnabled;
//    }
//
//    public boolean hasDefaultMetricsEnabled() {
//        return defaultMetricsEnabled;
//    }
//
//    @Override
//    public CloudService withDescription(String description) {
//        return (CloudService) super.withDescription(description);
//    }
//
//    @Override
//    public CloudService withName(String name) {
//        return (CloudService) super.withName(name);
//    }
//
//    @Override
//    public CloudService ofType(String type) {
//        return (CloudService) super.ofType(type);
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof CloudService)) return false;
//        if (!super.equals(o)) return false;
//
//        CloudService that = (CloudService) o;
//
//        if (serviceTemplates != null ? !serviceTemplates.equals(that.serviceTemplates) : that.serviceTemplates != null)
//            return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = super.hashCode();
//        result = 31 * result + (serviceTemplates != null ? serviceTemplates.hashCode() : 0);
//        return result;
//    }
//
//    @Override
//    public String toString() {
//        return "CloudService{" +
//                "serviceTemplates=" + serviceTemplates +
//                "} " + super.toString();
//    }
//
//
//}
