package at.ac.tuwien.dsg.elise.concepts.salsa.cloudservicestructure;

import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.elise.concepts.ServiceEntity;
import at.ac.tuwien.dsg.elise.concepts.mela.monitoringConcepts.Metric;
import at.ac.tuwien.dsg.elise.concepts.mela.monitoringConcepts.MetricValue;

public class ServiceIntance {
	String state;	
	private Map<Metric, MetricValue> runtimeProperties;
	
	private ServiceEntity hostOnEntity;	
	private List<ServiceEntity> connecttoEntity;

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
