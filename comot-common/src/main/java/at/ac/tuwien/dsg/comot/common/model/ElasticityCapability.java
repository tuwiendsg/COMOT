package at.ac.tuwien.dsg.comot.common.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author omoser
 */
public class ElasticityCapability extends AbstractCloudEntity {

    private String endpoint;

    private List<String> primitiveOperations;

    private List<CapabilityEffect> capabilityEffects;

    {
        primitiveOperations = new ArrayList<>();
        capabilityEffects = new ArrayList<>();
    }

    public enum CapabilityType {

        ScaleIn("scaleIn"),
        ScaleOut("scaleOut");

        private final String type;

        CapabilityType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    ElasticityCapability() {
        super(UUID.randomUUID().toString());
    }

    public static ElasticityCapability ElasticityCapability(String id) {
        return new ElasticityCapability();
    }

    public static ElasticityCapability ScaleIn() {
        return new ElasticityCapability().ofType(CapabilityType.ScaleIn);
    }

    public static ElasticityCapability ScaleOut() {
        return new ElasticityCapability().ofType(CapabilityType.ScaleOut);
    }

    public ElasticityCapability ofType(final CapabilityType capabilityType) {
        this.type = capabilityType.toString();
        return this;
    }

    public ElasticityCapability ofType(String customType) {
        this.type = customType;
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

    public ElasticityCapability withPrimitiveOperations(final List<String> primitiveOperations) {
        this.primitiveOperations = primitiveOperations;
        return this;
    }

    public ElasticityCapability withPrimitiveOperations(String... primitiveOperations) {
        this.primitiveOperations.addAll(Arrays.asList(primitiveOperations));
        return this;
    }

    public List<String> getPrimitiveOperations() {
        return primitiveOperations;
    }

    public List<CapabilityEffect> getCapabilityEffects() {
        return capabilityEffects;
    }

    public void setCapabilityEffects(List<CapabilityEffect> capabilityEffects) {
        this.capabilityEffects = capabilityEffects;
    }

    public ElasticityCapability withCapabilityEffects(final List<CapabilityEffect> capabilityEffects) {
        this.capabilityEffects = capabilityEffects;
        return this;
    }

    public ElasticityCapability withCapabilityEffects(CapabilityEffect... capabilityEffects) {
        this.capabilityEffects.addAll(Arrays.asList(capabilityEffects));
        return this;
    }

    public ElasticityCapability withCapabilityEffect(CapabilityEffect effect) {
        this.capabilityEffects.add(effect);
        return this;
    }
    
    
    

}
