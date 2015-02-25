package at.ac.tuwien.dsg.comot.m.ui.service;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.core.Coordinator;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.ui.mapper.SalsaOutputMapper;

// WADL http://localhost:8380/comot/rest/application.wadl
@Service
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Path("/services")
public class ServicesResource {

	private static final Logger log = LoggerFactory.getLogger(ServicesResource.class);

	// public static final String TOSCA_FILE = "./TOSCA-v1.0.xsd";

	@Autowired
	protected SalsaOutputMapper mapperOutput;
	@Autowired
	protected ToscaMapper mapperTosca;

	@Autowired
	protected Coordinator orchestrator;

	@PostConstruct
	public void startUp() {
		log.info("REST resource created");
	}

	@POST
	@Path("/")
	@Produces(MediaType.TEXT_PLAIN)
	public Response createService(Definitions def) throws CoreServiceException, ComotException, JAXBException {

		orchestrator.createCloudService(mapperTosca.createModel(def));
		return Response.ok(def.getId()).build();
	}

	@POST
	@Path("/{serviceId}/instances")
	@Produces(MediaType.TEXT_PLAIN)
	public Response createServiceInstance(@PathParam("serviceId") String serviceId) throws CoreServiceException,
			ComotException, ClassNotFoundException, IOException, JAXBException {

		String instanceId = orchestrator.createServiceInstance(serviceId);
		return Response.ok(instanceId).build();
	}

	@PUT
	@Path("/{serviceId}/instances/{instanceId}/eps/{epsId}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response assignSupportingEps(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId,
			@PathParam("epsId") String epsId) throws CoreServiceException, ComotException, ClassNotFoundException,
			IOException, JAXBException {

		orchestrator.assignSupportingOsu(instanceId, epsId);
		return Response.ok().build();
	}

	@PUT
	@Path("/{serviceId}/instances/{instanceId}/eps/{epsId}/events/{eventId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public Response triggerCustomEvent(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId,
			@PathParam("epsId") String epsId,
			@PathParam("eventId") String eventId,
			String optionalInput) throws CoreServiceException, ComotException, ClassNotFoundException, IOException,
			JAXBException {

		orchestrator.triggerCustomEvent(serviceId, instanceId, epsId, eventId, optionalInput);
		return Response.ok().build();
	}

	// update structure / requirements for monitoring / SYBL directives
	@PUT
	@Path("/{serviceId}")
	public Response updateService(@PathParam("serviceId") String serviceId, Definitions def) {

		// TODO
		return null;
	}

	// DELETE

	@DELETE
	@Path("/{serviceId}")
	public Response deleteService(@PathParam("serviceId") String serviceId) throws CoreServiceException,
			ComotException {

		// TODO
		return Response.ok().build();
	}

	@DELETE
	@Path("/{serviceId}/instances/{instanceId}")
	public Response deleteServiceInstance(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId) throws CoreServiceException, ComotException {

		// TODO
		return Response.ok().build();
	}

	@DELETE
	@Path("/{serviceId}/instances/{instanceId}/eps/{epsId}")
	public Response removeSupportingEps(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId,
			@PathParam("epsId") String epsId) throws CoreServiceException, ComotException {

		// TODO
		return Response.ok().build();
	}

	@GET
	@Path("/events/{serviceId}")
	@Consumes(SseFeature.SERVER_SENT_EVENTS)
	@Produces(SseFeature.SERVER_SENT_EVENTS)
	public EventOutput getServerSentEvents(
			@PathParam("serviceId") String serviceId) {

		final EventOutput eventOutput = new EventOutput();

		log.info("input: {}", serviceId);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {

					for (int i = 0; i < 10; i++) {
						log.info("sending {}", i);

						Thread.sleep(1000);

						final OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
						// eventBuilder.name("message-to-client");
						eventBuilder.data(String.class, "Hello world " + i + "!");
						final OutboundEvent event = eventBuilder.build();
						eventOutput.write(event);
					}

				} catch (IOException e) {
					throw new RuntimeException("Error when writing the event.", e);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						eventOutput.close();
					} catch (IOException ioClose) {
						throw new RuntimeException("Error when closing the event output.", ioClose);
					}
				}
			}
		}).start();

		return eventOutput;
	}

	// READ

	// @GET
	// @Consumes(MediaType.WILDCARD)
	// @Path("/")
	// public Response getServices() {
	//
	// List<ServiceEntity> list = orchestrator.getServices();
	// return Response.ok(list.toArray(new ServiceEntity[list.size()])).build();
	// }
	//
	// @GET
	// @Consumes(MediaType.WILDCARD)
	// @Path("/{serviceId}")
	// public Response getService(@PathParam("serviceId") String serviceId) throws CoreServiceException, ComotException
	// {
	//
	// CloudService service = orchestrator.getStatus(serviceId);
	// ElementState element = mapperOutput.extractOutput(service);
	// return Response.ok(element).build();
	// }
	//
	// // return status from salsa & monitoring (depends on what is turned on)
	// @GET
	// @Consumes(MediaType.WILDCARD)
	// @Path("/{serviceId}/state")
	// public Response getState(@PathParam("serviceId") String serviceId) throws CoreServiceException, ComotException {
	//
	// CloudService service = orchestrator.getStatus(serviceId);
	// ElementState element = mapperOutput.extractOutput(service);
	// return Response.ok(element).build();
	// }
	//
	// @GET
	// @Consumes(MediaType.WILDCARD)
	// @Path("/{serviceId}/monitoring/snapshots/last")
	// public Response getMonitoringData(@PathParam("serviceId") String serviceId) throws CoreServiceException,
	// ComotException {
	//
	// ElementMonitoring element = orchestrator.getMonitoringData(serviceId);
	// return Response.ok(element).build();
	// }
	//
	// @GET
	// @Consumes(MediaType.WILDCARD)
	// @Path("/{serviceId}/mcr")
	// public Response getMcr(@PathParam("serviceId") String serviceId) throws CoreServiceException {
	//
	// CompositionRulesConfiguration mcr = orchestrator.getMcr(serviceId);
	// return Response.ok(mcr).build();
	// }

}
