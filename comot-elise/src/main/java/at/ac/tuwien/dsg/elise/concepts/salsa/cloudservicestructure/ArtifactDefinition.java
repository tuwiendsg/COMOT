package at.ac.tuwien.dsg.elise.concepts.salsa.cloudservicestructure;

import at.ac.tuwien.dsg.elise.concepts.ResourceQualityOrCostEntity;

public class ArtifactDefinition extends ResourceQualityOrCostEntity {
		
		ArtifactRepoType artifactRepoType = ArtifactRepoType.DIRECT_URL;
		BuildType artifactBuildMethod = BuildType.NONE;
		String artifactRetrievingREF = "";
		
		public enum ArtifactRepoType{
			DIRECT_URL, GITHUB, NONE;
		}
		
		public enum BuildType{
			MAVEN, MAKE, NONE;
		}
		
		public ArtifactRepoType getArtifactRepoType() {
			return artifactRepoType;
		}

		public void setArtifactRepoType(ArtifactRepoType artifactRepoType) {
			this.artifactRepoType = artifactRepoType;
		}

		public BuildType getArtifactBuildMethod() {
			return artifactBuildMethod;
		}

		public void setArtifactBuildMethod(BuildType artifactBuildMethod) {
			this.artifactBuildMethod = artifactBuildMethod;
		}

		public String getArtifactRetrievingREF() {
			return artifactRetrievingREF;
		}

		public void setArtifactRetrievingREF(String artifactRetrievingREF) {
			this.artifactRetrievingREF = artifactRetrievingREF;
		}
}
