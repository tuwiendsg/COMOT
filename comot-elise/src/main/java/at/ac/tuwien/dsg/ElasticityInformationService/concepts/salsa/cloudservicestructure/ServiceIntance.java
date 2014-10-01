package at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.Entity;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.helper.PropertiesAdapter;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.monitoringConcepts.Metric;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.monitoringConcepts.MetricValue;

public class ServiceIntance {
	String state;	
	private Map<Metric, MetricValue> runtimeProperties;
	
	private Entity hostOnEntity;	
	private List<Entity> connecttoEntity;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Map<Metric, MetricValue> getRuntimeProperties() {
		return runtimeProperties;
	}

	public void setRuntimeProperties(Map<Metric, MetricValue> runtimeProperties) {
		this.runtimeProperties = runtimeProperties;
	}

}
