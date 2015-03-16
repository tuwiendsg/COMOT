/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.model.provider;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;

import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.comot.model.HasUniqueId;
import at.ac.tuwien.dsg.comot.recorder.BusinessId;

/**
 *
 * @author hungld
 *
 * This store the list of metric that associate to a resource/quality The list of these metric must be mapped by the resource/quality object
 */
@NodeEntity
public class ResourceOrQualityType extends Entity implements HasUniqueId, Serializable{

	private static final long serialVersionUID = -9039493125624891059L;

	public static final String ART_REFERENCE_TYPE = "URL";
	
    @Indexed(unique = true)
    @XmlAttribute
    @BusinessId
    protected String id;
    
    @Fetch
    protected Set<Metric> metrics;
    
    public ResourceOrQualityType(){
        
    }
    
    public ResourceOrQualityType(String name){
        this.name = name;
        this.id = "ResourceOrQualityType." + name;
    }
    
    public ResourceOrQualityType(String name, Set<Metric> metrics) {        
        this.metrics = metrics;
        this.name = name;
        this.id = "ResourceOrQualityType." + name;
    }

    public Metric getMetricByName(String name) {
        for (Metric metric : metrics) {
            if (metric.getName().equals(name)) {
                return metric;
            }
        }
        return null;
    }

    public void addMetric(Metric metric) {        
        if (this.metrics == null){
            this.metrics = new HashSet<>();
        }
        
        if (this.getMetricByName(metric.getName()) == null){
            System.out.println("Add a metric name: " + metric.getName() +" into ResourceOrQualityType: " + this.getId());
            this.metrics.add(metric);
        } else {
            System.out.println("Metric name exists: " + metric.getName() +" in ResourceOrQualityType: " + this.getId());
        }
    }

    // fluent call
    public ResourceOrQualityType hasMetric(Metric metric) {
        addMetric(metric);
        return this;
    }

    public Set<Metric> getMetrics() {
        return this.metrics;
    }

    @Override
    public String getId() {
        return id;
    }

	public void setId(String id) {
		this.id = id;
	}

	public void setMetrics(Set<Metric> metrics) {
		this.metrics = metrics;
	}
    
    
    
    
}
