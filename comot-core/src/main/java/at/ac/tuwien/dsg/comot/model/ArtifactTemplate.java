package at.ac.tuwien.dsg.comot.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author omoser
 */
public class ArtifactTemplate extends AbstractCloudEntity {

    private final Set<ArtifactReference> artifactReferences = new HashSet<>();

    public enum ArtifactType {
        Script("tosca:script");

        private final String type;

        ArtifactType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    ArtifactTemplate(String id) {
        super(id);
    }

    public static ArtifactTemplate ArtifactTemplate(String id) {
        return new ArtifactTemplate(id);
    }

    public static ArtifactTemplate ScriptArtifactTemplate(String id) {
        return new ArtifactTemplate(id).ofType(ArtifactType.Script);
    }

    public static ArtifactTemplate SingleScriptArtifactTemplate(String id, String scriptUri) {
        return new ArtifactTemplate(id).ofType(ArtifactType.Script)
                .consistsOf(ArtifactReference.ArtifactReference(scriptUri).locatedAt(scriptUri));
    }


    public ArtifactTemplate consistsOf(ArtifactReference... artifactReferences) {
        this.artifactReferences.addAll(Arrays.asList(artifactReferences));
        return this;
    }


    public Set<ArtifactReference> getArtifactReferences() {
        return artifactReferences;
    }

    @Override
    public ArtifactTemplate withDescription(String description) {
        return (ArtifactTemplate) super.withDescription(description);
    }

    @Override
    public ArtifactTemplate withName(String name) {
        return (ArtifactTemplate) super.withName(name);
    }

    @Override
    public ArtifactTemplate ofType(String type) {
        return (ArtifactTemplate) super.ofType(type);
    }

    public ArtifactTemplate ofType(ArtifactType type) {
        return (ArtifactTemplate) super.ofType(type.toString());
    }
}
