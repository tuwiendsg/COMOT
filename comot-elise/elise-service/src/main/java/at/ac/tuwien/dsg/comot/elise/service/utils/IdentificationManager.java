    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.utils;

import at.ac.tuwien.dsg.comot.model.elasticunit.identification.IdentificationDB;
import at.ac.tuwien.dsg.comot.model.elasticunit.identification.ServiceIdentification;
import at.ac.tuwien.dsg.comot.model.type.ServiceCategory;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author hungld
 */
public class IdentificationManager {

    static Logger logger = EliseConfiguration.logger;
    static ObjectMapper mapper = new ObjectMapper();
    static File storage = new File(EliseConfiguration.IDENTIFICATION_MAPPING_FILE);

    public static IdentificationDB load() {
        if (!storage.exists()) {
            try {
                storage.createNewFile();
            } catch (IOException ex) {
                logger.error("Cannot create identification DB. Error: " + ex);
            }
        } else {
            try {
                return (IdentificationDB) mapper.readValue(storage, IdentificationDB.class);
            } catch (IOException ex) {
                logger.debug("Identification DB is empty, create a new one! Message: " + ex);
            }
        }
        return new IdentificationDB();
    }

    public String searchAndUpdate(ServiceIdentification entityComposedID) {
        // search if there is an exist service identification that "equals" the the provided. Node: equals function is defined
        IdentificationDB currentDB = load();
        ServiceIdentification existSI = null;
        for (ServiceIdentification ite : currentDB.getIdentifications()) {
            if (entityComposedID.equals(ite)) {
                existSI = ite;
            }
        }

        // update        
        if (existSI == null) {  // if there is no existSI in the DB, create one
            System.out.println("There is no exist SI match with Identification, generating one...:" + entityComposedID);
            String uuid = UUID.randomUUID().toString();
            existSI = new ServiceIdentification(entityComposedID.getCategory());
            existSI.setUuid(uuid);
            currentDB.hasIdentification(existSI);
        }
        existSI.hasIdentificationItems(entityComposedID.getItems()); // then add more items

        // and save to file        
        try {
            mapper.writeValue(storage, currentDB);
        } catch (IOException ex) {
            logger.error("Cannot save Identification. Error: " + ex);
            ex.printStackTrace();            
        }
        return existSI.getUuid();

    }

  //  private synchronized void save(IdentificationDB idb)
//  {
//    IdentificationDB currentDB = load();
//    currentDB.getIdentifications().addAll(idb.getIdentifications());
//    try
//    {
//      mapper.writeValue(storage, currentDB);
//    }
//    catch (IOException ex)
//    {
//      logger.error("Cannot save Identification. Error: " + ex);
//    }
//  }
}
