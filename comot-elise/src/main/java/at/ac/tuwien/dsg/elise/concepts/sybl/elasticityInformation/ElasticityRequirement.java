package at.ac.tuwien.dsg.elise.concepts.sybl.elasticityInformation;

import at.ac.tuwien.dsg.elise.concepts.ServiceEntity;


public class ElasticityRequirement extends ServiceEntity{
	private SYBLAnnotation annotation;

	public SYBLAnnotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(SYBLAnnotation annotation) {
		this.annotation = annotation;
	}
}
