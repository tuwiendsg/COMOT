/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.impl;

import at.ac.tuwien.dsg.comot.elise.common.DAOInterface.OfferedServiceUnitDAO;
import at.ac.tuwien.dsg.comot.elise.common.DAOInterface.ProviderDAO;
import at.ac.tuwien.dsg.comot.elise.service.neo4jAccess.ProviderRepository;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Provider;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author hungld
 */
public class ProviderDAOImp implements ProviderDAO {
    /* MANAGE PROVIDERS */

    @Autowired
    ProviderRepository pdrepo;

    @Override
    public Provider getProviderByID(String uniqueID) {
        return pdrepo.findByUniqueID(uniqueID);
//        return new Provider("A new Provider", Provider.ProviderType.IAAS);
    }

    Logger logger = Logger.getLogger(OfferedServiceUnitDAOImp.class);
    
    @Autowired
    OfferedServiceUnitDAO offerServiceDAO;
    
    @Override
    public String addProvider(Provider provider) {
        if (pdrepo==null){
            logger.error("Cannot load ProviderRepository !");
            return null;
        }
        
        logger.debug("Prepare to add provider: ID=" + provider.getId() + ", GraphID: " + provider.getGraphID());
        if (provider.getOffering()!=null){
            logger.debug("This provider has " + provider.getOffering().size() + " OSU(s)");
        }
        for (OfferedServiceUnit u : provider.getOffering()) {
            logger.debug("Prepare to add offering: " + u.getId() + " - " + u.getCategory() + ", GraphID: " + u.getGraphID());
            offerServiceDAO.addOfferServiceUnitForProvider(u, provider.getId());
        }
        Provider r = pdrepo.save(provider);
        return "Saved the provider to graph with id: " + r.getGraphID();
    }

    @Override
    public Set<Provider> getProviders() {
        Set<Provider> providers = pdrepo.listProviders();
        return providers;
    }
    
    
    @Override
    public void test(){
        pdrepo.hashCode();
        if(pdrepo == null){
            logger.debug("pdREPO is null");
        } else {
            logger.debug("pdrepo is CREATED");
        }
                
    }
    
    

}
