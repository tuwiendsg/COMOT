/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.services;


import at.ac.tuwien.dsg.comot.elise.common.DataAccessInterface;
import at.ac.tuwien.dsg.comot.elise.neo4jAccess.EntityRepository;
import at.ac.tuwien.dsg.comot.elise.neo4jAccess.OfferedServiceRepository;
import at.ac.tuwien.dsg.comot.elise.neo4jAccess.ProviderRepository;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.Entity;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.MetricValue;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.Provider;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.Quality;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.Resource;
import at.ac.tuwien.dsg.comot.model.offeredserviceunit.ResourceOrQualityType;
import at.ac.tuwien.dsg.comot.model.runtimeserviceunit.UnitInstance;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import java.util.Set;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author hungld
 */
@Configuration
@EnableNeo4jRepositories
@Transactional
public class DataAccessImplementation extends Neo4jConfiguration implements DataAccessInterface {

    @Autowired GraphDatabaseService db;

    @Autowired OfferedServiceRepository surepo;    
    @Autowired ProviderRepository pdrepo;
    @Autowired EntityRepository enrepo;

    @Override
    public String health() {
        long num = surepo.count();
        System.out.println("Health checked");
        return "Elise service is running \n Number of SU: " + num;
    }
    
    @Override
    public String cleanDB(){
        surepo.cleanDataBase();
        return "DB Cleaned";
    }
    
    /* MANAGE OFFERED SERVICE UNIT */
    @Override
    public Set<OfferedServiceUnit> getOfferServiceUnits(){
        return surepo.listServiceUnit();
    }

    @Override
    public OfferedServiceUnit getOfferServiceUnitByID(String uniqueID) {
        System.out.println("Start to query a offered service unit with ID: " + uniqueID);
        return surepo.findByUniqueID(uniqueID);
    }
    
    @Override
    public Set<ResourceOrQualityType> getOfferServiceUnitResourceOrQualityTypeList(String uniqueID){
        System.out.println("getOfferServiceUnitResourceOrQualityTypeList");
        return surepo.findResourceOrQualityTypes(uniqueID);
    }
    
    @Override
    public Set<Resource> getOfferedServiceUnitResourceByType(String serviceID, String resourceTypeID){
        System.out.println("getOfferedServiceUnitResource: serviceID=" + serviceID +", resourceType: " + resourceTypeID);
        return surepo.findResourceOfOfferedServiceUnitByType(serviceID, resourceTypeID);
    }
    
    @Override
    public Resource getOfferedServiceUnitResourceByID(String serviceID, String resourceID){
        System.out.println("getOfferedServiceUnitResourceByID - ServiceID: " + serviceID +", resourceID: " + resourceID);
        
        // not only query resource but fetch metric also
        
        return surepo.findResourceOfOfferedServiceUnitByID(serviceID, resourceID);
    }
    
    @Override
    public Set<MetricValue> getResourceMetricDetails(String resourceTypeID){
        return surepo.getResourceMetricDetails(resourceTypeID);
    }
    

    @Override
    public String addOfferServiceUnit(OfferedServiceUnit unit, String providerID) {
        OfferedServiceUnit r = surepo.save(unit);
        return "Saved OSU to graph with id: " + r.getGraphID();
    }
    
    @Override
    public String addQualityForOfferedServiceUnit(String serviceUnitUniqueID, Quality quality) {
        OfferedServiceUnit osu = getOfferServiceUnitByID(serviceUnitUniqueID);
        osu.hasQuality(quality);
        return surepo.save(osu).getGraphID() +"";
    }
    
    @Override
    public Set<OfferedServiceUnit> getOfferedServiceOfProvider(String providerUniqueID){
        return surepo.findByProviderID(providerUniqueID);
    }
    
    /* MANAGE PROVIDERS */

    @Override
    public Provider getProviderByID(String uniqueID) {
        return pdrepo.findByUniqueID(uniqueID);        
//        return new Provider("A new Provider", Provider.ProviderType.IAAS);
    }

    @Override
    public String addProvider(Provider provider) {
        System.out.println("Add provider: ID=" + provider.getId() + ", GraphID: " + provider.getGraphID());
        for(OfferedServiceUnit u : provider.getOffering()){
            System.out.println("Offering: " + u.getId() +" - " + u.getCategory() + ", GraphID: " + u.getGraphID());
            for(Resource rs:u.getResources()){
                System.out.println("Resource: " + rs.getName() +", Graph id:" + rs.getGraphID());
            }
        }
        Provider r = pdrepo.save(provider);
        return "Saved OSU to graph with id: " + r.getGraphID();
    }

    @Override
    public Set<Provider> getProviders() {
        Set<Provider> providers = pdrepo.listProviders();
        return providers;
    }

    

    @Override
    public String addCloudServiceStructure(CloudService serviceStructure) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String addResourceForOfferedServiceUnit(String serviceUnitUniqueID, Resource resource) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCloudServiceStructureByID(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public UnitInstance getUnitInstance(String uniqueID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String addUnitInstance(UnitInstance unitInstance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String updateUnitInstance(UnitInstance unitInstance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String addEntityInGeneral(Entity entity) {
        System.out.println("Adding a generic entity...");
        return enrepo.save(entity).getGraphID() + "";
    }

   

    

}
