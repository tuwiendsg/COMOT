package at.ac.tuwien.dsg.comot.common.fluent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
		Script("sh");

		private final String type;

		ArtifactType(String type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return type;
		}
	}

	protected ArtifactTemplate(String id) {
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
