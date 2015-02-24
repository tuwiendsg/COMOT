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
 */
@NodeEntity
public class CostElement extends Entity {
   
    // this is for differentiating between multiple cost element of a cost function
    protected String type;
    
    // MetricValue: up to this measurement. This link to a metric and store value
    // Double: the cost is calculate as    
//    @RelatedTo
//    @Fetch
    MetricValue costIntervalFunction;    
    Double cost;
    
    public CostElement() {}
    
    public CostElement(String type, MetricValue costInterval, Double cost){
        this.type = type;
        this.costIntervalFunction = costInterval;
        this.cost = cost;
    }

    public String getType() {
        return type;
    }

    public MetricValue getCostIntervalFunction() {
        return costIntervalFunction;
    }

    public Double getCost() {
        return cost;
    }

	public void setType(String type) {
		this.type = type;
	}

	public void setCostIntervalFunction(MetricValue costIntervalFunction) {
		this.costIntervalFunction = costIntervalFunction;
	}

	public void setCost(Double cost) {
		this.cost = cost;
	}
    
    
    
}
