package at.ac.tuwien.dsg.comot.common.model;

/**
 * @author omoser
 */
public class ArtifactReference extends AbstractCloudEntity {

    private String uri;

    ArtifactReference(String id) {
        super(id);
    }

    public static ArtifactReference ArtifactReference(String id) {
        return new ArtifactReference(id);
    }

    public ArtifactReference withUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public ArtifactReference locatedAt(String uri) {
        return withUri(uri);
    }

    @Override
    public ArtifactReference withDescription(String description) {
        return (ArtifactReference) super.withDescription(description);
    }

    @Override
    public ArtifactReference withName(String name) {
        return (ArtifactReference) super.withName(name);
    }

    @Override
    public ArtifactReference ofType(String type) {
        return (ArtifactReference) super.ofType(type);
    }


    @Override
    public String toString() {
        return uri;
    }


}
