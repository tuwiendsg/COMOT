/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.common.DAOInterface;

import at.ac.tuwien.dsg.comot.model.provider.Provider;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author hungld
 */
@Path("/provider")
public interface ProviderDAO {
     /* PROVIDER ACCESS */
    @GET
    @Path("/{uniqueID}")
    @Produces(MediaType.APPLICATION_JSON)
    Provider getProviderByID(@PathParam("uniqueID") String uniqueID);

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    String addProvider(Provider provider);

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    Set<Provider> getProviders();
    
    @GET
    @Path("/test")
    void test();
}
