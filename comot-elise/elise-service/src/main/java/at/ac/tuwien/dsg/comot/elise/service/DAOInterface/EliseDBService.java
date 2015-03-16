/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.common.DAOInterface;

import at.ac.tuwien.dsg.comot.model.provider.Entity;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author hungld
 */
@Path("/db")
public interface EliseDBService {
    @GET
    @Path("/health")
    String health();

    @POST
    @Path("/clean")
    String cleanDB();
    
    /* Write entity in general */
    @POST
    @Path("/entity")
    @Consumes(MediaType.APPLICATION_JSON)
    String addEntityInGeneral(Entity entity);
}
