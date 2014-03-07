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

    private CloudEntity from;

    private CloudEntity to;

    EntityRelationship(String id) {
        super(id);
        context.put(id, this);
    }

    public static EntityRelationship EntityRelationship(String id) {
        return new EntityRelationship(id);
    }


    public EntityRelationship to(final CloudEntity target) {
        this.to = target;
        return this;
    }

    public EntityRelationship from(final CloudEntity source) {
        this.from = source;
        return this;
    }

    public EntityRelationship ofType(final RelationshipType type) {
        this.type = type.toString();
        return this;
    }

    public CloudEntity getFrom() {
        return from;
    }

    public CloudEntity getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "EntityRelationship{" +
                "type='" + type + '\'' +
                ", from=" + from +
                ", to=" + to +
                "} " + super.toString();
    }
}
