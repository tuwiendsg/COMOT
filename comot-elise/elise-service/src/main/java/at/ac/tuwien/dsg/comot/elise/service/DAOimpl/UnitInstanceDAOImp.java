/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.DAOimpl;

import at.ac.tuwien.dsg.comot.elise.common.DAOInterface.UnitInstanceDAO;
import at.ac.tuwien.dsg.comot.elise.common.EliseManager.EliseManager;
import at.ac.tuwien.dsg.comot.elise.common.message.EliseQuery;
import at.ac.tuwien.dsg.comot.elise.common.message.EliseQueryRule;
import at.ac.tuwien.dsg.comot.elise.service.neo4jAccess.UnitInstanceRepository;
import at.ac.tuwien.dsg.comot.elise.service.utils.EliseConfiguration;
import at.ac.tuwien.dsg.comot.elise.service.utils.RunCollector;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.MetricValue;
import at.ac.tuwien.dsg.comot.model.elasticunit.identification.ServiceIdentification;
import at.ac.tuwien.dsg.comot.model.elasticunit.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.ServiceCategory;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author hungld
 */
public class UnitInstanceDAOImp implements UnitInstanceDAO {

    Logger logger = EliseConfiguration.logger;

    @Autowired
    UnitInstanceRepository repo;

    @Override
    public Set<UnitInstance> getUnitInstanceList() {
        RunCollector.RunAllCollector();
        return repo.listUnitInstance();
    }

    @Override
    public String addUnitInstance(UnitInstance unitInstance) {
        logger.debug("Save UnitInstance: " + unitInstance.getName());

        String uuid = updateComposedIdentification(unitInstance);
        logger.debug("The service is assign UUID: " + uuid);

        UnitInstance existedInstance = this.repo.findByUniqueID(unitInstance.getId());
        
        logger.debug("SHOULD PASS THIS LINE 1");
        // if unit is in data base, merge and save new one 
        // TODO: check Neo4j for better query to update node
        if (existedInstance != null) {
            logger.debug("Merging service ....");
            unitInstance.mergeWith(existedInstance);
            logger.debug("Deleting unit... ");
            this.repo.delete(existedInstance);
//            this.repo.deleteUnitByID(existedInstance.getId());
            logger.debug("Deleting done...");
        }
        
        logger.debug("Start saving. Json: " + unitInstance.toJson());
        
        

        UnitInstance u = repo.save(unitInstance);
        
        logger.debug("Saved ...");
        if (u != null) {
            logger.debug("Saved unit instance:" + u.getId() + ", name: " + u.getName());
            return u.getId();
        } else {
            logger.debug("Fail to save unit instance: " + unitInstance.getId() + ", name: " + unitInstance.getName());
            return null;
        }
    }

    @Override
    public Set<UnitInstance> queryUnitInstance(EliseQuery query) {
        RunCollector.RunAllCollector();        
        
        logger.debug("Starting to query and filter the unit instances...");        
        
        Set<UnitInstance> instances = repo.findByCategory(query.getCategory().toString());
        logger.debug("Found " + instances.size() + " of the category: " + query.getCategory().toString());
//        int rulefulfill = 0;    // 0: N/A, 1: fulfill, -1: violate
//        for (UnitInstance u : instances) {
//            logger.debug("Checking instance: " + u.getId()+"/"+u.getName());
//            for (MetricValue value : u.findAllMetricValues()) {
//                for (EliseQueryRule rule : query.getRules()) {
//                    logger.debug("Comparing unit(" + value.getName() +"="+value.getValue() +" with the rule " + rule.toString() );
//                    if (value.getName().equals(rule.getMetric())) {  // if the metric name is match
//                        if (rule.isFulfilled(value.getValue())) {    // check if value is fulfill
//                            logger.debug("One rule fulfilled !");
//                            rulefulfill += 1;                           // add one to the counting of fulfilled value
//                        } else {
//                            logger.debug("A rule is violated ! BREAK !");
//                            rulefulfill -= 1;                           // or reduce it and break as a rule is violated
//                            break;
//                        }
//                    }
//                }
//                // if all the condition is fulfill, fulfill = rule.size()                
//                if (rulefulfill == query.getRules().size()) {           // if rules are fulfilled
//                    logger.debug("Fullfill all rules, now checking unit instance if fullfill. Fulfilled count: " + rulefulfill + " with no. of rules: " + query.getRules().size());
//                    Set<String> capas = u.findAllPrimitiveNames();       // also check capability
//                    if (capas.containsAll(query.getHasCapabilities())) {
//                        instances.add(u);
//                    }
//                }
//            }
//
//        }
        return instances;
    }

    @Override
    public UnitInstance getUnitInstanceByID(String uniqueID) {
        RunCollector.RunAllCollector();
        return repo.findByUniqueID(uniqueID);
    }

    @Override
    public Set<String> getUnitCategory() {
        Set<String> set = new HashSet();
        for (ServiceCategory c : ServiceCategory.values()) {
            set.add(c.toString());
        }
        return set;
    }

    private String updateComposedIdentification(UnitInstance instance) {
        EliseManager collectorService = ((EliseManager) JAXRSClientFactory.create(EliseConfiguration.getRESTEndpointLocal(), EliseManager.class, Collections.singletonList(new JacksonJsonProvider())));
        ServiceIdentification si = ServiceIdentification.fromJson(instance.getIdentification());
        String uuid = collectorService.updateComposedIdentification(si);
        if ((uuid == null) || (uuid.isEmpty())) {
            this.logger.error("Cannot get the UUID of the composed-identification. That is impossible to happen !");
            return null;
        }
        instance.setId(uuid);
        return uuid;
    }

}
