/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.collectormanager;

/**
 *
 * @author hungld
 */
public class CollectorDescription {
    
    // the ID which the collector is collecting 
    String entityID;
    
    // the collector send information of instance or 
    EntityType type;
    
    
    
    
    enum EntityType{
        offeredServiceUnit,
        unitInstance
    }
}
