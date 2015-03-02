/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.impl;

import at.ac.tuwien.dsg.comot.elise.common.EliseInterface.CollectorManager;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hungld
 */
public class CollectorManagerImp implements CollectorManager{

    Map<String, String> collectorIDs = new HashMap<>();

    @Override
    public String registerCollectorForEntity(String collectorID, String entityID) {
        return collectorIDs.put(collectorID, entityID);
    }

    @Override
    public String registerCollectorNoEntity(String collectorID) {
        return collectorIDs.put(collectorID, null);
    }

    @Override
    public String removeCollector(String collectorID) {
        return collectorIDs.remove(collectorID);
    }

    @Override
    public Map<String, String> getCollectorList() {
        return this.collectorIDs;
    }
    
    
    
       
}
