package at.ac.tuwien.dsg.comot.m.common.model.monitoring;

public class Metric {

	protected String name;
	protected String measurementUnit;
	protected MetricType metricType;
	protected Object value;
	protected ValueType valueType;

	public enum MetricType {
		RESOURCE,
		COST,
		QUALITY,
		ELASTICITY
	}

	public enum ValueType {
		NUMERIC,
		TEXT,
		ENUMERATION
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMeasurementUnit() {
		return measurementUnit;
	}

	public void setMeasurementUnit(String measurementUnit) {
		this.measurementUnit = measurementUnit;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public MetricType getMetricType() {
		return metricType;
	}

	public void setMetricType(MetricType metricType) {
		this.metricType = metricType;
	}

	public ValueType getValueType() {
		return valueType;
	}

	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}

}
