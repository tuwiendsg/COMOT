package at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.Link;

public class ServiceUnitRequirement extends Link{
	
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
	
	public ServiceUnitRequirement() {}
	
	public ServiceUnitRequirement(String name, DependencyType dependencyType){
		this.name=  name;
		this.dependencyType = dependencyType;
		this.restriction = DependencyRestriction.MANDATORY;
	}
	
	public ServiceUnitRequirement(String name, DependencyType dependencyType, DependencyRestriction restriction){
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
