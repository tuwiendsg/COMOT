package at.ac.tuwien.dsg.comot.model;

/**
 * @author omoser
 */
public class EntityRelationship extends AbstractCloudEntity {

    public enum RelationshipType {

        ConnectedTo("CONNECTTO"),
        HostedOn("HOSTON");

        private String type;

        RelationshipType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    private CloudEntity source;

    private CloudEntity target;

    public EntityRelationship(String id) {
        super(id);
    }

    public static EntityRelationship EntityRelationship(String id) {
        return new EntityRelationship(id);
    }


    public EntityRelationship to(final CloudEntity target) {
        this.target = target;
        return this;
    }

    public EntityRelationship from(final CloudEntity source) {
        this.source = source;
        return this;
    }

    public EntityRelationship ofType(final RelationshipType type) {
        this.type = type.toString();
        return this;
    }

    @Override
    public String toString() {
        return "EntityRelationship{" +
                "type='" + type + '\'' +
                ", source=" + source +
                ", target=" + target +
                "} " + super.toString();
    }
}
