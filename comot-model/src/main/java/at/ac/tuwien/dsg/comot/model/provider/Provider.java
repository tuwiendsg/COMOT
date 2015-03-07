/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.model.provider;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.comot.model.HasUniqueId;
import at.ac.tuwien.dsg.comot.recorder.BusinessId;

/**
 *
 * @author hungld
 */
@NodeEntity
public class Provider extends Entity implements HasUniqueId{
    
    @Indexed(unique = true)
    @BusinessId
    protected String id;
  
    protected ProviderType providerType = ProviderType.IAAS;
    
    protected Set<OfferedServiceUnit> offering;
    
    {
        //id = UUID.randomUUID().toString();
        offering = new HashSet<>();        
    }
    
    public Provider() {}

    public Provider(String name) {
        this.name = name;
        this.id = name;
    }
    
    public Provider(String name, ProviderType type) {
        this.name = name;
        this.providerType = type;        
        this.id = name;
    }
    
    // fluent
    public Provider hasOfferedServiceUnit(OfferedServiceUnit offered){
        addOfferedServiceUnit(offered);
        return this;
    }
    
    public void addOfferedServiceUnit(OfferedServiceUnit offered){
        this.offering.add(offered);
    }
        
    public ProviderType getProviderType() {
        return providerType;
    }

    public Set<OfferedServiceUnit> getOffering() {
        return offering;
    }

    @Override
    public String getId() {
        return this.id;
    }
    
    public void setId(String id){
        this.id=id;
    }
    
    public enum ProviderType {
        IAAS, PAAS,
        CUSTOM;
    }

	public void setProviderType(ProviderType providerType) {
		this.providerType = providerType;
	}

	public void setOffering(Set<OfferedServiceUnit> offering) {
		this.offering = offering;
	}
    
    
}
