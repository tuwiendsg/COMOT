/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.model.provider;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 *
 * @author hungld
 */
@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
public abstract class Entity implements Serializable{
    
	private static final long serialVersionUID = 4164778764692932101L;

	@GraphId 
    protected Long graphID;
    
    // Name is the unique identify in local context,
    // e.g. in one service unit, a resource has a unique name
    @XmlAttribute
    protected String name;
    
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
