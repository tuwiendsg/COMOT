package at.ac.tuwien.dsg.comot.m.ui.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import org.glassfish.jersey.media.sse.SseFeature;
import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.core.Coordinator;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.InformationServiceMock;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycle;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleFactory;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.Transition;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.ui.UiAdapter;
import at.ac.tuwien.dsg.comot.m.ui.model.Lc;
import at.ac.tuwien.dsg.comot.m.ui.model.LcState;
import at.ac.tuwien.dsg.comot.m.ui.model.ServiceAndInstances;
import at.ac.tuwien.dsg.comot.m.ui.model.ServiceInstanceUi;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.type.State;

// WADL http://localhost:8380/comot/rest/application.wadl
@Service
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Path("/manager")
public class ServicesResource {

	private static final Logger log = LoggerFactory.getLogger(ServicesResource.class);

	// public static final String TOSCA_FILE = "./TOSCA-v1.0.xsd";

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected ToscaMapper mapperTosca;
	@Autowired
	protected LifeCycleManager lcManager;

	@Autowired
	protected Coordinator coordinator;
	@Autowired
	protected InformationServiceMock infoServ;

	@PostConstruct
	public void startUp() {
		log.info("REST resource created");
	}

	@POST
	@Path("/services")
	@Produces(MediaType.TEXT_PLAIN)
	public Response createService(Definitions def) throws CoreServiceException, ComotException, JAXBException,
			ClassNotFoundException, IOException {

		coordinator.createCloudService(mapperTosca.createModel(def));
		return Response.ok(def.getId()).build();
	}

	@DELETE
	@Path("/services/{serviceId}")
	public Response deleteService(@PathParam("serviceId") String serviceId) throws CoreServiceException,
			ComotException {

		// TODO
		return Response.ok().build();
	}

	@POST
	@Path("/services/{serviceId}/instances")
	@Produces(MediaType.TEXT_PLAIN)
	public Response createServiceInstance(@PathParam("serviceId") String serviceId) throws CoreServiceException,
			ComotException, ClassNotFoundException, IOException, JAXBException {

		String instanceId = coordinator.createServiceInstance(serviceId);
		return Response.ok(instanceId).build();
	}

	@DELETE
	@Path("/services/{serviceId}/instances/{instanceId}")
	public Response deleteServiceInstance(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId) throws CoreServiceException, ComotException {

		// TODO
		return Response.ok().build();
	}

	@PUT
	@Path("/services/{serviceId}/instances/{instanceId}/start")
	@Produces(MediaType.TEXT_PLAIN)
	public Response startServiceInstance(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId) throws CoreServiceException,
			ComotException, ClassNotFoundException, IOException, JAXBException {

		coordinator.startServiceInstance(serviceId, instanceId);
		return Response.ok().build();
	}

	@PUT
	@Path("/services/{serviceId}/instances/{instanceId}/stop")
	@Produces(MediaType.TEXT_PLAIN)
	public Response stopServiceInstance(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId) throws CoreServiceException,
			ComotException, ClassNotFoundException, IOException, JAXBException {

		coordinator.stopServiceInstance(serviceId, instanceId);
		return Response.ok().build();
	}

	@PUT
	@Path("/services/{serviceId}/instances/{instanceId}/eps/{epsId}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response assignSupportingEps(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId,
			@PathParam("epsId") String epsId) throws CoreServiceException, ComotException, ClassNotFoundException,
			IOException, JAXBException {

		coordinator.assignSupportingOsu(serviceId, instanceId, epsId);
		return Response.ok().build();
	}

	@DELETE
	@Path("/services/{serviceId}/instances/{instanceId}/eps/{epsId}")
	public Response removeSupportingEps(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId,
			@PathParam("epsId") String epsId) throws CoreServiceException, ComotException, ClassNotFoundException,
			IOException, JAXBException {

		coordinator.removeAssignmentOfSupportingOsu(serviceId, instanceId, epsId);
		return Response.ok().build();
	}

	@PUT
	@Path("/services/{serviceId}/instances/{instanceId}/eps/{epsId}/events/{eventName}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public Response triggerCustomEvent(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId,
			@PathParam("epsId") String epsId,
			@PathParam("eventName") String eventName,
			String optionalInput) throws CoreServiceException, ComotException, ClassNotFoundException, IOException,
			JAXBException {

		// log.info(">>>{}<<<", optionalInput);

		coordinator.triggerCustomEvent(serviceId, instanceId, epsId, eventName, optionalInput);
		return Response.ok().build();
	}

	// update structure / requirements for monitoring / SYBL directives
	@PUT
	@Path("/services/{serviceId}")
	public Response updateService(@PathParam("serviceId") String serviceId, Definitions def) {

		// TODO
		return null;
	}

	@GET
	@Path("/services/{serviceId}/instances/{instanceId}/events")
	@Consumes(SseFeature.SERVER_SENT_EVENTS)
	@Produces(SseFeature.SERVER_SENT_EVENTS)
	public EventOutput getServerSentEvents(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId) throws InterruptedException, IOException {

		final EventOutput eventOutput = new EventOutput();

		UiAdapter adapter = context.getBean(UiAdapter.class);
		adapter.setUiAdapter(instanceId, eventOutput);
		adapter.startAdapter("UI_" + UUID.randomUUID().toString());
		adapter.checkClient();

		return eventOutput;
	}

	// READ

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/services/lifecycle/{level}")
	public Response getLifeCycle(@PathParam("level") Type level) {

		LifeCycle lifeCycle = LifeCycleFactory.getLifeCycle(level);
		Lc lc = new Lc();

		for (State tempS : lifeCycle.getStates()) {
			lc.getStates().add(new LcState(tempS));
		}

		for (Transition tr : lifeCycle.getTransitions()) {
			lc.addTransition(tr.getState(), tr.getAction(), tr.getNextState());
		}

		return Response.ok(lc).build();
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/services/allInstances")
	public Response getAllInstances() {

		Map<String, List<String>> map = infoServ.getAllInstanceIds();
		ServiceAndInstances[] array = new ServiceAndInstances[map.keySet().size()];
		int i = 0;

		for (String serviceId : map.keySet()) {
			array[i] = new ServiceAndInstances(serviceId, map.get(serviceId));
			i++;
		}

		log.info("" + array);

		return Response.ok(array).build();
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/services/eps")
	public Response getElasticPlatformServices() {

		List<OfferedServiceUnit> list = new ArrayList<>(infoServ.getOsus().values());

		return Response.ok(list.toArray(new OfferedServiceUnit[list.size()])).build();
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/services")
	public Response getServices() throws ClassNotFoundException, IOException {
		List<CloudService> list = new ArrayList<>(infoServ.getServices().values());
		return Response.ok(list.toArray(new CloudService[list.size()])).build();
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/services/{serviceId}/instances/{instanceId}")
	public Response getServicesInstance(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId) throws ClassNotFoundException, IOException {

		ServiceInstanceUi instanceUi = new ServiceInstanceUi(
				instanceId,
				infoServ.getServiceInstance(serviceId, instanceId),
				lcManager.getCurrentState(instanceId));

		return Response.ok(instanceUi).build();
	}

}
