package at.ac.tuwien.dsg.ElasticityInformationService.concepts.sybl.elasticityInformation;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.Entity;


public class ElasticityRequirement extends Entity{
	private SYBLAnnotation annotation;

	public SYBLAnnotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(SYBLAnnotation annotation) {
		this.annotation = annotation;
	}
}
