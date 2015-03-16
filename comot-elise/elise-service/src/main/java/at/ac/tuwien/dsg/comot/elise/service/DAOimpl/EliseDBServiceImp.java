/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.impl;

import at.ac.tuwien.dsg.comot.elise.common.DAOInterface.EliseDBService;
import at.ac.tuwien.dsg.comot.elise.service.neo4jAccess.EntityRepository;
import at.ac.tuwien.dsg.comot.elise.service.neo4jAccess.OfferedServiceRepository;
import at.ac.tuwien.dsg.comot.elise.service.neo4jAccess.ProviderRepository;
import at.ac.tuwien.dsg.comot.model.provider.Entity;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Provider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author hungld
 */
public class EliseDBServiceImp implements EliseDBService {

    @Autowired
    OfferedServiceRepository surepo;
    
    @Autowired
    EntityRepository enrepo;
    
    @Autowired
    ProviderRepository providerRepo;

    @Override
    public String health() {
        long num = surepo.count();
        System.out.println("Health checked");
        return "Elise service is running \n Number of SU: " + num;
    }

    @Override
    public String cleanDB() {
        surepo.cleanDataBase();
        return "DB Cleaned";
    }

    @Override
    public String addEntityInGeneral(Entity entity) {
        System.out.println("Adding a generic entity...");
        
        if (entity.getClass().equals(Provider.class)){
            providerRepo.save((Provider) entity);
        } else if (entity.getClass().equals(OfferedServiceUnit.class)){
            surepo.save((OfferedServiceUnit) entity);
        }
                
        return enrepo.save(entity).getName()+ "";
    }
}
