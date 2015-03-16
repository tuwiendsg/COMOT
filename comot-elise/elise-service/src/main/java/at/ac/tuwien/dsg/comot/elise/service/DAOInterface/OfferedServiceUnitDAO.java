/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.common.DAOInterface;

import at.ac.tuwien.dsg.comot.model.provider.MetricValue;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.Quality;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.provider.ResourceOrQualityType;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
@Path("/offeredserviceunit")
public interface OfferedServiceUnitDAO {

    /* OFFERED SERVICE UNIT ACCESS */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    Set<OfferedServiceUnit> getOfferServiceUnits();

    @GET
    @Path("/{uniqueID}")
    @Produces(MediaType.APPLICATION_JSON)
    OfferedServiceUnit getOfferServiceUnitByID(@PathParam("uniqueID") String uniqueID);

    @GET
    @Path("/{uniqueID}/resourceorqualitytype")
    @Produces(MediaType.APPLICATION_JSON)
    Set<ResourceOrQualityType> getOfferServiceUnitResourceOrQualityTypeList(@PathParam("uniqueID") String uniqueID);

    @GET
    @Path("/{uniqueID}/resourcestype/{resourceTypeID}/resources")
    @Produces(MediaType.APPLICATION_JSON)
    Set<Resource> getOfferedServiceUnitResourceByType(@PathParam("uniqueID") String uniqueID, @PathParam("resourceTypeID") String resourceTypeID);

    @GET
    @Path("/{uniqueID}/resource/{resourceID}")
    @Produces(MediaType.APPLICATION_JSON)
    Resource getOfferedServiceUnitResourceByID(@PathParam("uniqueID") String uniqueID, @PathParam("resourceID") String resourceTypeID);

    @GET
    @Path("/resource/{resourceID}")
    @Produces(MediaType.APPLICATION_JSON)
    Set<MetricValue> getResourceMetricDetails(@PathParam("resourceID") String resourceTypeID);

    @POST
    @Path("/provider/{providerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    String addOfferServiceUnitForProvider(OfferedServiceUnit unit, @PathParam("providerId") String providerID);

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    String updateOfferServiceUnit(OfferedServiceUnit unit);

    @POST
    @Path("/{uniqueID}/resource")
    @Consumes(MediaType.APPLICATION_JSON)
    String addResourceForOfferedServiceUnit(@PathParam("uniqueID") String serviceUnitUniqueID, Resource resource);

    @POST
    @Path("/{uniqueID}/quality")
    @Consumes(MediaType.APPLICATION_JSON)
    String addQualityForOfferedServiceUnit(@PathParam("uniqueID") String serviceUnitUniqueID, Quality quality);

    @GET
    @Path("/provider/{providerUniqueID}")
    @Produces(MediaType.APPLICATION_JSON)
    Set<OfferedServiceUnit> getOfferedServiceOfProvider(@PathParam("providerUniqueID") String providerUniqueID);

    @GET
    @Path("/test")
    void test();

}
