/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.impl;

import at.ac.tuwien.dsg.comot.elise.common.DAOInterface.OfferedServiceUnitDAO;
import at.ac.tuwien.dsg.comot.elise.service.neo4jAccess.EntityRepository;
import at.ac.tuwien.dsg.comot.elise.service.neo4jAccess.OfferedServiceRepository;
import at.ac.tuwien.dsg.comot.model.provider.Metric;
import at.ac.tuwien.dsg.comot.model.provider.MetricValue;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Quality;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.provider.ResourceOrQualityType;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;

/**
 *
 * @author hungld
 */
//@Configuration
//@EnableNeo4jRepositories
//@Transactional
//public class OfferedServiceUnitDAOImp extends Neo4jConfiguration implements OfferedServiceUnitDAO {
public class OfferedServiceUnitDAOImp implements OfferedServiceUnitDAO {
    // for saving generic thing
        
     @Autowired
    Neo4jOperations template;
    

    Logger logger = Logger.getLogger(OfferedServiceUnitDAOImp.class);

//     @Bean(destroyMethod = "shutdown")
//    public GraphDatabaseService graphDatabaseService() {
//        return new GraphDatabaseFactory().newEmbeddedDatabase(EliseConfiguration.DATA_BASE_STORAGE);
//        //return new SpringRestGraphDatabase(EliseConfiguration.DATA_BASE_SEPARATE_ENDPOINT);
//    }
//    @Autowired GraphDatabaseService db;
    @Autowired
    OfferedServiceRepository surepo;

    @Autowired
    EntityRepository enrepo;

    /* MANAGE OFFERED SERVICE UNIT */
    @Override
    public Set<OfferedServiceUnit> getOfferServiceUnits() {
        return surepo.listServiceUnit();
    }

    @Override
    public OfferedServiceUnit getOfferServiceUnitByID(String uniqueID) {
        System.out.println("Start to query a offered service unit with ID: " + uniqueID);
        return surepo.findByUniqueID(uniqueID);
    }

    @Override
    public Set<ResourceOrQualityType> getOfferServiceUnitResourceOrQualityTypeList(String uniqueID) {
        System.out.println("getOfferServiceUnitResourceOrQualityTypeList");
        return surepo.findResourceOrQualityTypes(uniqueID);
    }

    @Override
    public Set<Resource> getOfferedServiceUnitResourceByType(String serviceID, String resourceTypeID) {
        System.out.println("getOfferedServiceUnitResource: serviceID=" + serviceID + ", resourceType: " + resourceTypeID);
        return surepo.findResourceOfOfferedServiceUnitByType(serviceID, resourceTypeID);
    }

    @Override
    public Resource getOfferedServiceUnitResourceByID(String serviceID, String resourceID) {
        System.out.println("getOfferedServiceUnitResourceByID - ServiceID: " + serviceID + ", resourceID: " + resourceID);

        // not only query resource but fetch metric also
        return surepo.findResourceOfOfferedServiceUnitByID(serviceID, resourceID);
    }

    @Override
    public Set<MetricValue> getResourceMetricDetails(String resourceTypeID) {
        return surepo.getResourceMetricDetails(resourceTypeID);
    }

    @Override
    public String addOfferServiceUnitForProvider(OfferedServiceUnit unit, String providerID) {
        surepo.hashCode();
        if (surepo == null) {
            logger.debug("surepo is NULL");
            return null;
        } else {
            logger.debug("surepo is CREATED");
        }

        if (unit == null) {
            logger.error("Error when adding OSU, OSU is null !");
            return null;
        }
        logger.debug("Prepare to add OSU: " + unit.getName() + ", ID: " + unit.getId());
        if (unit.getResources() == null || unit.getResources().isEmpty()) {
            logger.debug("Unit " + unit.getName() + " has no resource to add!");
        } else {
            for (Resource rs : unit.getResources()) {
                logger.debug("Prepare to add resource: " + rs.getName() + ", id: " + rs.getId() + " for OSU: " + unit.getName());
                addResourceForOfferedServiceUnit(unit.getId(), rs);
            }
        }

        OfferedServiceUnit r = surepo.save(unit);
        return "Saved OSU to graph with id: " + r.getGraphID();
    }

    @Override
    public String addQualityForOfferedServiceUnit(String serviceUnitUniqueID, Quality quality) {
        OfferedServiceUnit osu = getOfferServiceUnitByID(serviceUnitUniqueID);
        osu.hasQuality(quality);
        return surepo.save(osu).getGraphID() + "";
    }

    @Override
    public Set<OfferedServiceUnit> getOfferedServiceOfProvider(String providerUniqueID) {
        return surepo.findByProviderID(providerUniqueID);
    }

    @Override
    public String addResourceForOfferedServiceUnit(String serviceUnitUniqueID, Resource resource) {
        // TODO: add nested resource

        // add metric
//        surepo.save(resource);
        return "not support yet";
    }

    @Override
    public String updateOfferServiceUnit(OfferedServiceUnit unit) {
        //save all metricType first
        for (Resource rs : unit.getResources()) {
            addResourceForOfferedServiceUnit(unit.getId(), rs);
        }
        return surepo.save(unit) + "";
    }

    @Override
    public void test() {
        surepo.hashCode();
        if (surepo == null) {
            logger.debug("surepo is NULL");
        } else {
            logger.debug("surepo is CREATED");
        }

        enrepo.hashCode();
        if (enrepo == null) {
            logger.debug("enrepo is NULL");
        } else {
            logger.debug("enrepo is CREATED");
        }

    }

}
