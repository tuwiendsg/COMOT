package at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.annotation.RelatedToVia;
import org.springframework.data.neo4j.fieldaccess.DynamicProperties;
import org.springframework.data.neo4j.fieldaccess.DynamicPropertiesContainer;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.Entity;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.LinkType;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure.ServiceUnitRequirement.DependencyRestriction;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure.ServiceUnitRequirement.DependencyType;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.sybl.elasticityInformation.ElasticityRequirement;

@NodeEntity
public class ServiceUnit extends Entity{	
	private static final long serialVersionUID = 1L;
	
	// artifact definition
	ArtifactRepoType artifactRepoType = ArtifactRepoType.DIRECT_URL;
	BuildType artifactBuildMethod = BuildType.NONE;
	String artifactRetrievingREF = "";
	
		
	public enum ArtifactRepoType{
		DIRECT_URL, GITHUB, NONE;
	}
	
	public enum BuildType{
		MAVEN, MAKE, NONE;
	}
	
	
	Set<PrimitiveOperation> primitiveOperations= new HashSet<PrimitiveOperation>();
	
	Set<String> deploymentCapabilities = new HashSet<String>();	
	
		
	/** BELOW IS THE LINK DEFINITION **/
	
	// The set of links
	@RelatedToVia(type = LinkType.SERVICE_UNIT_DEPLOYMENT_DEPENDENCY, direction = Direction.OUTGOING)
	@Fetch
	Set<ServiceUnitRequirement> deploymentRequirements = new HashSet<ServiceUnitRequirement>();	// more complex with type and restriction
	
	// The set of related nodes which this node require
	@RelatedTo(type = LinkType.SERVICE_UNIT_DEPLOYMENT_DEPENDENCY, direction=Direction.OUTGOING)
	@Fetch
	Set<ServiceUnit>  relatedServiceunits = new HashSet<ServiceUnit>();
	
	// The set of related nodes which depend on this node
	@RelatedTo(type = LinkType.SERVICE_UNIT_DEPLOYMENT_DEPENDENCY, direction=Direction.INCOMING)
	@Fetch
	Set<ServiceUnit> dependOnMeDeployment = new HashSet<ServiceUnit>();
	
	// for rSYBL
	Set<ElasticityRequirement> elasticityRequirement = new HashSet<ElasticityRequirement>();	// SYBL
	
	public ServiceUnitRequirement requireOtherServiceUnit(ServiceUnit su, String name, DependencyType type, DependencyRestriction restriction){
		ServiceUnitRequirement req = new ServiceUnitRequirement(name, type, restriction);
		req.setSource(this);
		req.setTarget(su);
		this.deploymentRequirements.add(req);
		return req;
	}
	
	public ServiceUnit(){}
	
	
	@Deprecated
	public String toXMLString() {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(this.getClass());
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			StringWriter sw = new StringWriter();
			jaxbMarshaller.marshal(this, sw);
			return sw.toString();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public void addPrimitiveOperation(PrimitiveOperation op){
		this.primitiveOperations.add(op);
	}
	
	
	
	public void addRequirement(ServiceUnitRequirement req){
		this.deploymentRequirements.add(req);		
	}
	
	public void addCapability(String capa){		
		this.deploymentCapabilities.add(capa);
	}

	public Set<String> getDeploymentCapabilities() {
		return deploymentCapabilities;
	}

	public Set<PrimitiveOperation> getPrimitiveOperations() {
		return primitiveOperations;
	}

	public void setPrimitiveOperations(Set<PrimitiveOperation> primitiveOperations) {
		this.primitiveOperations = primitiveOperations;
	}

	public void setElasticityRequirement(Set<ElasticityRequirement> elasticityRequirement) {
		this.elasticityRequirement = elasticityRequirement;
	}

	public void setDeploymentCapabilities(Set<String> deploymentCapabilities) {
		this.deploymentCapabilities = deploymentCapabilities;
	}

	public Set<ServiceUnitRequirement> getDeploymentRequirements() {
		return deploymentRequirements;
	}

	public void setDeploymentRequirements(Set<ServiceUnitRequirement> deploymentRequirements) {
		this.deploymentRequirements = deploymentRequirements;
	}

	public Set<ServiceUnit> getRelatedServiceunits() {
		return relatedServiceunits;
	}

	public void setRelatedServiceunits(Set<ServiceUnit> relatedServiceunits) {
		this.relatedServiceunits = relatedServiceunits;
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
