package at.ac.tuwien.dsg.elise.concepts.salsa.cloudservicestructure;

import org.springframework.data.neo4j.annotation.RelationshipEntity;

import at.ac.tuwien.dsg.elise.concepts.Link;
import at.ac.tuwien.dsg.elise.concepts.LinkType;

@RelationshipEntity(type=LinkType.SERVICE_UNIT_DEPLOYMENT_DEPENDENCY)
public class ServiceUnitDeploymentDependency extends Link{
	
	String name;
	DependencyType dependencyType;
	DependencyRestriction restriction;
	
	public enum DependencyType{
		HOSTED_ON,
		ASSOCIATED_AT_RUNTIME,
		PEER_OF;
	}
	
	public enum DependencyRestriction{
		MANDATORY, OPTIONAL, CONFLICT;
	}
	
	public ServiceUnitDeploymentDependency() {}
	
	public ServiceUnitDeploymentDependency(String name, DependencyType dependencyType){
		this.name=  name;
		this.dependencyType = dependencyType;
		this.restriction = DependencyRestriction.MANDATORY;
	}
	
	public ServiceUnitDeploymentDependency(String name, DependencyType dependencyType, DependencyRestriction restriction){
		this.name = name;
		this.dependencyType = dependencyType;
		this.restriction = restriction;
	}
	
	public DependencyType getDependencyType() {
		return dependencyType;
	}

	public void setType(DependencyType dependencyType) {
		this.dependencyType = dependencyType;
	}

	public DependencyRestriction getRestriction() {
		return restriction;
	}

	public void setRestriction(DependencyRestriction restriction) {
		this.restriction = restriction;
	}

}
