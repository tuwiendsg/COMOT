package at.ac.tuwien.dsg.ElasticityInformationService.concepts.sybl.elasticityInformation;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.ServiceEntity;


public class ElasticityRequirement extends ServiceEntity{
	private SYBLAnnotation annotation;

	public SYBLAnnotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(SYBLAnnotation annotation) {
		this.annotation = annotation;
	}
}
