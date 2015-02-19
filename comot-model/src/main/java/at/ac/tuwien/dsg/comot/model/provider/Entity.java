/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.model.provider;

import at.ac.tuwien.dsg.comot.model.HasUniqueId;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 *
 * @author hungld
 */

@NodeEntity
public abstract class Entity {
    
    @GraphId Long graphID;
    
    // Name is the unique identify in local context,
    // e.g. in one service unit, a resource has a unique name
    String name;
    
    public Entity(){}
    
    public Entity(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Long getGraphID() {
        return graphID;
    }

    public void setName(String name) {
        this.name = name;
    }
    

 
      
}
