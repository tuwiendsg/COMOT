package at.ac.tuwien.dsg.comot.common.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author omoser
 */
public class ArtifactTemplate extends AbstractCloudEntity {

    private final Set<ArtifactReference> artifactReferences = new HashSet<>();

    protected BundleConfig bundleConfig;

    public BundleConfig getBundleConfig() {
        return bundleConfig;
    }

    public enum ArtifactType {

        RunOnceScript("sh"),
        RunContinuousScript("shcont"),
        WarArtifact("war"),
        DockerFile("dockerfile"),
        MiscArtifact("misc");

        private final String type;

        ArtifactType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    protected ArtifactTemplate() {
        super("Artifact_" + UUID.randomUUID());
    }

    protected ArtifactTemplate(String id) {
        super(id);
    }

    public static ArtifactTemplate ArtifactTemplate() {
        return new ArtifactTemplate();
    }

//    public static ArtifactTemplate ScriptArtifactTemplate() {
//        return new ArtifactTemplate().ofType(ArtifactType.RunOnceScript);
//    }
//    public static ArtifactTemplate ScriptArtifactTemplate(String id) {
//        return new ArtifactTemplate(id).ofType(ArtifactType.RunOnceScript);
//    }

    public static ArtifactTemplate SingleScriptArtifact(String scriptUri) {
        return new ArtifactTemplate().ofType(ArtifactType.RunOnceScript)
                .consistsOf(ArtifactReference.ArtifactReference(scriptUri).locatedAt(scriptUri));
    }
    public static ArtifactTemplate SingleScriptArtifact(String id, String scriptUri) {
        return new ArtifactTemplate(id).ofType(ArtifactType.RunOnceScript)
                .consistsOf(ArtifactReference.ArtifactReference(scriptUri).locatedAt(scriptUri));
    }
    
    public static ArtifactTemplate WarArtifact(String warUri) {
        return new ArtifactTemplate().ofType(ArtifactType.WarArtifact)
                .consistsOf(ArtifactReference.ArtifactReference(warUri).locatedAt(warUri));
    }
    public static ArtifactTemplate WarArtifact(String id, String warUri) {
        return new ArtifactTemplate(id).ofType(ArtifactType.WarArtifact)
                .consistsOf(ArtifactReference.ArtifactReference(warUri).locatedAt(warUri));
    }
    
    public static ArtifactTemplate ServiceArtifact(String scriptUri) {
        return new ArtifactTemplate().ofType(ArtifactType.RunContinuousScript)
                .consistsOf(ArtifactReference.ArtifactReference(scriptUri).locatedAt(scriptUri));
    }
    public static ArtifactTemplate ServiceArtifact(String id, String scriptUri) {
        return new ArtifactTemplate(id).ofType(ArtifactType.RunContinuousScript)
                .consistsOf(ArtifactReference.ArtifactReference(scriptUri).locatedAt(scriptUri));
    }
    
    public static ArtifactTemplate MiscArtifact(String id, String artifactURI) {
        return new ArtifactTemplate(id).ofType(ArtifactType.MiscArtifact)
                .consistsOf(ArtifactReference.ArtifactReference(artifactURI).locatedAt(artifactURI));
    }
    
    public static ArtifactTemplate DockerFileArtifact(String id, String dockerFileURI) {
        return new ArtifactTemplate(id).ofType(ArtifactType.DockerFile)
                .consistsOf(ArtifactReference.ArtifactReference(dockerFileURI).locatedAt(dockerFileURI));
    }

    public ArtifactTemplate consistsOf(ArtifactReference... artifactReferences) {
        this.artifactReferences.addAll(Arrays.asList(artifactReferences));
        return this;
    }

    public ArtifactTemplate withBundleConfig(final BundleConfig bundleConfig) {
        this.bundleConfig = bundleConfig;
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
