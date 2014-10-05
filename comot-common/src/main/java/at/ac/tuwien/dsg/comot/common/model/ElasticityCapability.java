package at.ac.tuwien.dsg.comot.common.model;

/**
 * @author omoser
 */
public class ElasticityCapability extends AbstractCloudEntity {

    private String endpoint;

    public enum CapabilityType {

        ScaleIn("scalein"),
        ScaleOut("scaleout");

        private final String type;

        CapabilityType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    ElasticityCapability(String id) {
        super(id);
    }

    public static ElasticityCapability ElasticityCapability(String id) {
        return new ElasticityCapability(id);
    }

    public static ElasticityCapability ScaleIn(String id) {
        return new ElasticityCapability(id).ofType(CapabilityType.ScaleIn);
    }

    public static ElasticityCapability ScaleOut(String id) {
        return new ElasticityCapability(id).ofType(CapabilityType.ScaleOut);
    }

    public ElasticityCapability ofType(final CapabilityType capabilityType) {
        this.type = capabilityType.toString();
        return this;
    }

    public ElasticityCapability withEndpoint(final String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public ElasticityCapability withType(final CapabilityType type) {
        this.type = type.toString();
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

}
