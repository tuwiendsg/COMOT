/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.common.EliseInterface;

import java.util.Map;
import java.util.Set;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 *
 * @author hungld
 */
@Path("/collector")
public interface CollectorManager {
    // for collector
    @POST
    @Path("/{collectorID}/entity/{entityID}")
    String registerCollectorForEntity(@PathParam("collectorID") String collectorID, @PathParam("entityID") String entityID);
    
    @POST
    @Path("/{collectorID}")
    String registerCollectorNoEntity(@PathParam("collectorID") String collectorID);
    
    @DELETE
    @Path("/{collectorID}")
    String removeCollector(@PathParam("collectorID") String collectorID);
    
    @GET
    @Path("/")
    Map<String,String> getCollectorList();
            
    
}
