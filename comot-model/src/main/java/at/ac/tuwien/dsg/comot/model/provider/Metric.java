/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.model.provider;

import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 *
 * @author hungld
 *
 * Each metric has an unique ID This class extends Entity just for using EntityRepository, not for semantic of the model
 */
@NodeEntity
public class Metric extends Entity {

//    protected String name;
    protected String measurementUnit;
    protected MetricType metricType;

    public Metric() {
    }

    public Metric(String name, String measurementUnit, MetricType metricType) {
        this.name = name;
        this.measurementUnit = measurementUnit;
        this.metricType = metricType;
    }

    public enum MetricType {

        RESOURCE, QUALITY, COST, ELASTICITY
    }

    public String getName() {
        return this.name;
    }

    public String getMeasurementUnit() {
        return measurementUnit;
    }

    public MetricType getMetricType() {
        return metricType;
    }

	public void setMeasurementUnit(String measurementUnit) {
		this.measurementUnit = measurementUnit;
	}

	public void setMetricType(MetricType metricType) {
		this.metricType = metricType;
	}
    
    
    
    
}
