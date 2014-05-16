package at.ac.tuwien.dsg.comot;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * @author omoser
 */
@Service
public class NoopSalsaEngineApi implements ComotSalsaEngineApiInterface {

    private Set<String> serviceIds = new ConcurrentHashSet<>();

    private Set<String> instanceIds = new ConcurrentHashSet<>();

    @Override
    @PUT
    @Path("/services/{serviceName}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deployService(@PathParam("serviceName") String serviceName, InputStream inputStream) {
        String serviceId = UUID.randomUUID().toString();
        URI location = UriBuilder.fromMethod(this.getClass(), "deployService").build(serviceId);
        serviceIds.add(serviceId);
        return Response.created(location).entity(serviceId).build();
    }

    @Override
    @POST
    @Path("/services/{serviceId}/topologies/{topologyId}/nodes/{nodeId}/instance-count/{quantity}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response spawnInstance(@PathParam("serviceId") String serviceId,
                                  @PathParam("topologyId") String topologyId,
                                  @PathParam("nodeId") String nodeId,
                                  @PathParam("quantity") int quantity) {

        List<String> instances = new ArrayList<>();
        for (int i = 0; i < quantity; ++i) {
            String instanceId = UUID.randomUUID().toString();
            instances.add(instanceId);
            instanceIds.add(instanceId);
        }

        return Response.ok(instances.toArray()).build();
    }

    @Override
    @DELETE
    @Path("/services/{serviceId}/topologies/{topologyId}/nodes/{nodeId}/instances/{instanceId}")
    public Response destroyInstance(@PathParam("serviceId") String serviceId,
                                    @PathParam("topologyId") String topologyId,
                                    @PathParam("nodeId") String nodeId,
                                    @PathParam("instanceId") String instanceId) {

        if (!instanceIds.contains(instanceId)) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No such instance: " + instanceId).build());
        }

        return Response.ok().build();
    }

    @Override
    @DELETE
    @Path("/services/{serviceId}")
    public Response undeployService(@PathParam("serviceId") String serviceId) {
        if (!serviceIds.contains(serviceId)) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No such service: " + serviceId).build());
        }

        return Response.ok().build();
    }

    @Override
    @GET
    @Path("/services/{serviceId}")
    @Produces("application/json")
    public Response fetchStatus(@PathParam("serviceId") String serviceId) {
        String mockResponse;
        try {
            mockResponse = FileUtils.readFileToString(new ClassPathResource("salsa-status-response.json").getFile());
        } catch (IOException e) {
            e.printStackTrace();
            throw new WebApplicationException();
        }

        return Response.ok().entity(mockResponse).build();
    }
}
