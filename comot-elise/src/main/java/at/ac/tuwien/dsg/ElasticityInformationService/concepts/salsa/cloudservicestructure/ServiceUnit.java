package at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.annotation.RelatedToVia;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.PrimitiveOperation;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.ServiceEntity;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.LinkType;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure.Links.ServiceUnitDeploymentDependency;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure.Links.ServiceUnitDeploymentDependency.DependencyRestriction;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure.Links.ServiceUnitDeploymentDependency.DependencyType;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure.configurationTypes.ArtifactDefinition;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure.enums.ServiceUnitType;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.sybl.elasticityInformation.ElasticityRequirement;

import com.fasterxml.jackson.annotation.JsonIgnore;

@NodeEntity
@TypeAlias("ServiceUnit")
public class ServiceUnit extends ServiceEntity{	
	private static final long serialVersionUID = -8767519259218020488L;
	
	{
		this.type="ServiceUnit";
	}
	
	ServiceUnitType serviceUnitType;
	
	ArtifactDefinition artifact = new ArtifactDefinition();
	
	Set<String> deploymentCapabilities = new HashSet<String>();
	
		
	/** BELOW IS THE LINK DEFINITION **/
	
	// The set of links
	@RelatedToVia(type = LinkType.SERVICE_UNIT_DEPLOYMENT_DEPENDENCY, direction = Direction.OUTGOING)
	@Fetch
	@JsonIgnore
	Set<ServiceUnitDeploymentDependency> deploymentRequirements = new HashSet<ServiceUnitDeploymentDependency>();	// more complex with type and restriction
	
	// The set of related nodes which this node require
	@RelatedTo(type = LinkType.SERVICE_UNIT_DEPLOYMENT_DEPENDENCY, direction=Direction.OUTGOING)
	@Fetch
	@JsonIgnore
	Set<ServiceUnit>  requiredServiceunits = new HashSet<ServiceUnit>();

	// The set of related nodes which depend on this node
	@RelatedTo(type = LinkType.SERVICE_UNIT_DEPLOYMENT_DEPENDENCY, direction=Direction.INCOMING)
	@Fetch
	@JsonIgnore
	Set<ServiceUnit> dependentDeployment = new HashSet<ServiceUnit>();	

	// for rSYBL
	@JsonIgnore
	Set<ElasticityRequirement> elasticityRequirement = new HashSet<ElasticityRequirement>();	// SYBL
	
	
	
	public ServiceUnitDeploymentDependency requireOtherServiceUnit(ServiceUnit su, String name, DependencyType type, DependencyRestriction restriction){
		ServiceUnitDeploymentDependency req = new ServiceUnitDeploymentDependency(name, type, restriction);
		req.setSource(this);
		req.setTarget(su);
		//this.deploymentRequirements.add(req);
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
	
	
	
//	public void addRequirement(ServiceUnitRequirement req){
//		this.deploymentRequirements.add(req);		
//	}
	
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

//	public void setElasticityRequirement(Set<ElasticityRequirement> elasticityRequirement) {
//		this.elasticityRequirement = elasticityRequirement;
//	}

	public void setDeploymentCapabilities(Set<String> deploymentCapabilities) {
		this.deploymentCapabilities = deploymentCapabilities;
	}

//	public Set<ServiceUnitRequirement> getDeploymentRequirements() {
//		return deploymentRequirements;
//	}
//
//	public void setDeploymentRequirements(Set<ServiceUnitRequirement> deploymentRequirements) {
//		this.deploymentRequirements = deploymentRequirements;
//	}

//	public Set<ServiceUnit> getRelatedServiceunits() {
//		return relatedServiceunits;
//	}
//
//	public void setRelatedServiceunits(Set<ServiceUnit> relatedServiceunits) {
//		this.relatedServiceunits = relatedServiceunits;
//	}
	
	
		
}
