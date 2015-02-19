/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.common;

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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
//import org.springframework.stereotype.Service;

/**
 *
 * @author hungld Using for both client stub and server implementation
 */
//@Service
@Path("/elise")
//@Produces(MediaType.APPLICATION_JSON)
//@Consumes(MediaType.APPLICATION_JSON)
public interface DataAccessInterface {

    @GET
    @Path("/health")
    String health();

    @POST
    @Path("/db/clean")
    String cleanDB();

    /* OFFERED SERVICE UNIT ACCESS */
    @GET
    @Path("/offeredserviceunit")
    @Produces(MediaType.APPLICATION_JSON)
    Set<OfferedServiceUnit> getOfferServiceUnits();
    
    @GET
    @Path("/offeredserviceunit/{uniqueID}")
    @Produces(MediaType.APPLICATION_JSON)
    OfferedServiceUnit getOfferServiceUnitByID(@PathParam("uniqueID") String uniqueID);
    
    @GET
    @Path("/offeredserviceunit/{uniqueID}/resourceorqualitytype")
    @Produces(MediaType.APPLICATION_JSON)
    Set<ResourceOrQualityType> getOfferServiceUnitResourceOrQualityTypeList(@PathParam("uniqueID") String uniqueID);
    
    @GET
    @Path("/offeredserviceunit/{serviceID}/resourcestype/{resourceTypeID}/resources")
    @Produces(MediaType.APPLICATION_JSON)
    Set<Resource> getOfferedServiceUnitResourceByType(@PathParam("serviceID") String serviceID, @PathParam("resourceTypeID") String resourceTypeID);
    
    @GET
    @Path("/offeredserviceunit/{serviceID}/resource/{resourceID}")
    @Produces(MediaType.APPLICATION_JSON)
    Resource getOfferedServiceUnitResourceByID(@PathParam("serviceID") String serviceID, @PathParam("resourceID") String resourceTypeID);
    
    @GET
    @Path("/offeredserviceunit/resource/{resourceID}")
    @Produces(MediaType.APPLICATION_JSON)
    Set<MetricValue> getResourceMetricDetails(@PathParam("resourceID") String resourceTypeID);

    @POST
    @Path("/offeredserviceunit")
    @Consumes(MediaType.APPLICATION_JSON)
    String addOfferServiceUnit(OfferedServiceUnit unit, @QueryParam("providerId") String providerID);

    @POST
    @Path("/offeredserviceunit/{uniqueID}/resource")
    @Consumes(MediaType.APPLICATION_JSON)
    String addResourceForOfferedServiceUnit(@PathParam("uniqueID") String serviceUnitUniqueID, Resource resource);

    @POST
    @Path("/offeredserviceunit/{uniqueID}/quality")
    @Consumes(MediaType.APPLICATION_JSON)
    String addQualityForOfferedServiceUnit(@PathParam("uniqueID") String serviceUnitUniqueID, Quality quality);    

    @GET
    @Path("/offeredserviceunit/provider/{providerUniqueID}")
    @Produces(MediaType.APPLICATION_JSON)
    Set<OfferedServiceUnit> getOfferedServiceOfProvider(@PathParam("providerUniqueID") String providerUniqueID);
    
    

    /* PROVIDER ACCESS */
    @GET
    @Path("/provider/{uniqueID}")
    @Produces(MediaType.APPLICATION_JSON)
    Provider getProviderByID(@PathParam("uniqueID") String uniqueID);

    @POST
    @Path("/provider")
    @Consumes(MediaType.APPLICATION_JSON)
    String addProvider(Provider provider);

    @GET
    @Path("/provider")
    @Produces(MediaType.APPLICATION_JSON)
    Set<Provider> getProviders();

    /* Cloud service structure management */
    @POST
    @Path("/cloudservice")
    @Consumes(MediaType.APPLICATION_JSON)
    String addCloudServiceStructure(CloudService serviceStructure);

    @GET
    @Path("/cloudservice/{uniqueID}")
    String getCloudServiceStructureByID(@PathParam("uniqueID") String id);

    /* Service instance management */
    @GET
    @Path("/unitinstance")
    @Produces(MediaType.APPLICATION_JSON)
    UnitInstance getUnitInstance(String uniqueID);

    @POST
    @Path("/unitinstance/{uniqueID}")
    @Consumes(MediaType.APPLICATION_JSON)
    String addUnitInstance(UnitInstance unitInstance);

    @PUT
    @Path("/unitinstance/{uniqueID}")
    @Consumes(MediaType.APPLICATION_JSON)
    String updateUnitInstance(UnitInstance unitInstance);

    /* Write entity in general */
    @POST
    @Path("/entity")
    @Consumes(MediaType.APPLICATION_JSON)
    String addEntityInGeneral(Entity entity);

}
