package at.ac.tuwien.dsg.ElasticityInformationService.concepts.sybl.elasticityInformation;

import java.io.Serializable;

public class ElasticityMetric{
	private String metricName = "";
	private Object value;
	private String measurementUnit="";
	public String getMetricName() {
		return metricName;
	}
	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String getMeasurementUnit() {
		return measurementUnit;
	}
	public void setMeasurementUnit(String measurementUnit) {
		this.measurementUnit = measurementUnit;
	}
}
