package at.ac.tuwien.dsg.comot.model;

/**
 * @author omoser
 */
public class Capability extends AbstractCloudEntity {

    public enum CapabilityType {
        Variable("variable");

        private final String type;

        CapabilityType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }


    Capability(String id) {
        super(id);
    }

    public static Capability Capability(String id) {
        return new Capability(id);
    }

    public static Capability Variable(String id) {
        return new Capability(id).ofType(CapabilityType.Variable);
    }

    @Override
    public Capability withName(final String name) {
        this.name = name;
        return this;
    }

    public Capability withType(final CapabilityType capabilityType) {
        this.type = capabilityType.toString();
        return this;
    }

    public Capability ofType(final CapabilityType capabilityType) {
        this.type = capabilityType.toString();
        return this;
    }

    @Override
    public Capability withType(final String type) {
        this.type = type;
        return this;
    }

    @Override
    public Capability withDescription(final String description) {
        this.description = description;
        return this;
    }
}
