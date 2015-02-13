package at.ac.tuwien.dsg.comot.common.model;

/**
 * @author omoser
 */
public class Requirement extends AbstractCloudEntity {

    public enum RequirementType {

        Variable("variable");

        private final String type;

        RequirementType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    Requirement(String id) {
        super(id);
    }

    public static Requirement Requirement(String id) {
        return new Requirement(id);
    }

    public static Requirement Variable(String id) {
        return new Requirement(id).ofType(RequirementType.Variable);
    }

    @Override
    public Requirement withDescription(String description) {
        return (Requirement) super.withDescription(description);
    }

    @Override
    public Requirement withName(String name) {
        return (Requirement) super.withName(name);
    }

    @Override
    public Requirement ofType(String type) {
        return (Requirement) super.ofType(type);
    }

    public Requirement ofType(RequirementType type) {
        return ofType(type.toString());
    }
}
