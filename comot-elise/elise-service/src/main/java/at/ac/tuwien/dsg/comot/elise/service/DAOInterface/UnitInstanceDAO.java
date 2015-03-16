/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.common.DAOInterface;

import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author hungld
 */
@Path("/unitinstance")
public interface UnitInstanceDAO {
    

    /* Service instance management */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    UnitInstance getUnitInstance(String uniqueID);

    @POST
    @Path("/{uniqueID}")
    @Consumes(MediaType.APPLICATION_JSON)
    String addUnitInstance(UnitInstance unitInstance);

    @PUT
    @Path("/{uniqueID}")
    @Consumes(MediaType.APPLICATION_JSON)
    String updateUnitInstance(UnitInstance unitInstance);
}
