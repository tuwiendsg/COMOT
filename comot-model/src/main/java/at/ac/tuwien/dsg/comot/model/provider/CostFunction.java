/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.model.provider;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 *
 * @author hungld
 */
@NodeEntity
public class CostFunction extends Entity{
    
//    @RelatedTo
    protected Set<Entity> appliedInConjunctionWith;    
    
//    @RelatedTo
    protected Set<CostElement> costElements;
    
    public CostFunction() {
    }

    public CostFunction(String name) {
        this.name = name;
    }
    
    public void assignToEntity(Entity seviceResourceOrQuality){
        this.appliedInConjunctionWith.add(seviceResourceOrQuality);
    }

    public Set<Entity> getAppliedInConjunctionWith() {
        return appliedInConjunctionWith;
    }

    public Set<CostElement> getCostElements() {
        return costElements;
    }
    
    public void addCostElement(CostElement costElement){
        if (this.costElements==null){
            costElements = new HashSet<>();
        }
        this.costElements.add(costElement);
    }
    
    public void conjunctWith(Entity entity){
        if (appliedInConjunctionWith == null){
            appliedInConjunctionWith = new HashSet<>();
        }
        this.appliedInConjunctionWith.add(entity);
    }

	public void setAppliedInConjunctionWith(Set<Entity> appliedInConjunctionWith) {
		this.appliedInConjunctionWith = appliedInConjunctionWith;
	}

	public void setCostElements(Set<CostElement> costElements) {
		this.costElements = costElements;
	}
    
    
    
    
}
