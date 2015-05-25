/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.collector.rsybl;

import at.ac.tuwien.dsg.comot.elise.collector.UnitInstanceCollector;
import at.ac.tuwien.dsg.comot.model.elasticunit.identification.ServiceIdentification;
import at.ac.tuwien.dsg.comot.model.elasticunit.runtime.UnitInstance;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungld
 */
public class EliseCollector  extends UnitInstanceCollector {
    String syblREST="http://localhost:8180/rSYBL/restWS";   // just a default
      static Logger logger = LoggerFactory.getLogger("SALSACollector");
    public EliseCollector() {
        super("rSYBL-unit-collector");
        this.syblREST = readAdaptorConfig("endpoint");
    }

    @Override
    public Set<UnitInstance> collect() {
        logger.debug("SALSA collector start to collect unit instance from SALSA !");
        Set<UnitInstance> instances = new HashSet<>();
        // get all the service list
        Client orderClient = ClientBuilder.newClient();
        WebTarget target = orderClient.target(this.syblREST+"/elasticservices");
        String serviceList= target.request().get(String.class);
        for(String s: serviceList.split(",")){
            if (!s.trim().equals("")){
                // query for service instance information
                WebTarget target2 = orderClient.target(this.syblREST+"/"+s+"/description");
            }
        }
        return null;    // NOT DONE YET
    }

    @Override
    public ServiceIdentification identify(UnitInstance paramUnitInstance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
