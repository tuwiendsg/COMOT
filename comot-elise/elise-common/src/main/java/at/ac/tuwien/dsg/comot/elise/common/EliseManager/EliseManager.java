/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.common.EliseManager;

import at.ac.tuwien.dsg.comot.model.elasticunit.identification.ServiceIdentification;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author hungld
 */
@Path("/manager")
public interface EliseManager {

    // for collector
  @POST
  @Path("/collector")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.TEXT_PLAIN)
  public String registerCollector(CollectorDescription paramCollectorDescription);
  
  @PUT
  @Path("/collector")
  @Consumes(MediaType.APPLICATION_JSON)
  public String updateColector(CollectorDescription paramCollectorDescription);
  
  @GET
  @Path("/collector{collectorID}")
  @Produces(MediaType.APPLICATION_JSON)
  public CollectorDescription getColector(@PathParam("collectorID") String paramString);
  
  @DELETE
  @Path("/collector{collectorID}")
  @Produces(MediaType.TEXT_PLAIN)
  public String removeCollector(@PathParam("collectorID") String paramString);
  
  @GET
  @Path("/collector")
  @Produces(MediaType.APPLICATION_JSON)
  public List<CollectorDescription> getCollectorList();
  
  
    
//  @POST
//  @Path("/data")
//  @Consumes({"application/json"})
//  public abstract String recievedCollectorData(CollectorData paramCollectorData);
    
    // for identification
    
    @POST
    @Path("/identification")
    @Consumes(MediaType.APPLICATION_JSON)
    String updateComposedIdentification(ServiceIdentification si);
    

    // health and generic stuff
    @GET
    @Path("/health")
    String health();

    @POST
    @Path("/clean")
    String cleanDB();

}
