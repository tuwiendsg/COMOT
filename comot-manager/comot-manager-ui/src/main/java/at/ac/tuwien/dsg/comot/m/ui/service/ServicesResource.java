package at.ac.tuwien.dsg.comot.m.ui.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.SseFeature;
import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import at.ac.tuwien.dsg.comot.m.adapter.general.Manager;
import at.ac.tuwien.dsg.comot.m.adapter.general.SingleQueueManager;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.core.Coordinator;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycle;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleFactory;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleManager;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.LifeCycleTransition;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.m.ui.UiAdapter;
import at.ac.tuwien.dsg.comot.m.ui.model.Lc;
import at.ac.tuwien.dsg.comot.m.ui.model.LcState;
import at.ac.tuwien.dsg.comot.m.ui.model.ServiceAndInstances;
import at.ac.tuwien.dsg.comot.m.ui.model.ServiceInstanceUi;
import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceEntity;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;
import at.ac.tuwien.dsg.comot.model.type.DirectiveType;
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
	protected InformationClient infoServ;

	@javax.annotation.Resource
	public Environment env;

	@PostConstruct
	public void startUp() {
		log.info("REST resource created");
	}

	@POST
	@Path("/services")
	@Produces(MediaType.TEXT_PLAIN)
	public Response createService(Definitions def) throws EpsException, ComotException, JAXBException,
			ClassNotFoundException, IOException {

		coordinator.createCloudService(mapperTosca.createModel(def));
		return Response.ok(def.getId()).build();
	}

	@DELETE
	@Path("/services/{serviceId}")
	public Response deleteService(@PathParam("serviceId") String serviceId) throws EpsException,
			ComotException {

		// TODO
		return Response.ok().build();
	}

	@POST
	@Path("/services/{serviceId}/instances")
	@Produces(MediaType.TEXT_PLAIN)
	public Response createServiceInstance(@PathParam("serviceId") String serviceId) throws EpsException,
			ComotException, ClassNotFoundException, IOException, JAXBException {

		String instanceId = coordinator.createServiceInstance(serviceId);
		return Response.ok(instanceId).build();
	}

	@DELETE
	@Path("/services/{serviceId}/instances/{instanceId}")
	public Response deleteServiceInstance(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId) throws EpsException, ComotException {

		// TODO
		return Response.ok().build();
	}

	@PUT
	@Path("/services/{serviceId}/instances/{instanceId}/start")
	@Produces(MediaType.TEXT_PLAIN)
	public Response startServiceInstance(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId) throws EpsException,
			ComotException, ClassNotFoundException, IOException, JAXBException {

		coordinator.startServiceInstance(serviceId, instanceId);
		return Response.ok().build();
	}

	@PUT
	@Path("/services/{serviceId}/instances/{instanceId}/stop")
	@Produces(MediaType.TEXT_PLAIN)
	public Response stopServiceInstance(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId) throws EpsException,
			ComotException, ClassNotFoundException, IOException, JAXBException {

		coordinator.stopServiceInstance(serviceId, instanceId);
		return Response.ok().build();
	}

	@PUT
	@Path("/services/{serviceId}/instances/{instanceId}/kill")
	@Produces(MediaType.TEXT_PLAIN)
	public Response kill(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId) throws EpsException,
			ComotException, ClassNotFoundException, IOException, JAXBException {

		coordinator.kill(serviceId, instanceId);
		return Response.ok().build();
	}

	@PUT
	@Path("/services/{serviceId}/instances/{instanceId}/eps/{epsId}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response assignSupportingEps(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId,
			@PathParam("epsId") String epsId) throws EpsException, ComotException, ClassNotFoundException,
			IOException, JAXBException {

		coordinator.assignSupportingOsu(serviceId, instanceId, epsId);
		return Response.ok().build();
	}

	@DELETE
	@Path("/services/{serviceId}/instances/{instanceId}/eps/{epsId}")
	public Response removeSupportingEps(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId,
			@PathParam("epsId") String epsId) throws EpsException, ComotException, ClassNotFoundException,
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
			String optionalInput) throws EpsException, ComotException, ClassNotFoundException, IOException,
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

	@PUT
	@Path("/services/{serviceId}/instances/{instanceId}/elasticity")
	public Response reconfigureElasticity(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId,
			CloudService service) throws AmqpException, JAXBException, EpsException {

		Navigator nav = new Navigator(service);

		for (ServiceEntity entity : nav.getAllServiceEntities()) {
			for (SyblDirective dir : entity.getDirectives()) {
				if (dir.getType() == null) {

					String preProcessed = dir.getDirective().trim().toUpperCase();

					if (preProcessed.startsWith("STRATEGY")) {
						dir.setType(DirectiveType.STRATEGY);
					} else if (preProcessed.startsWith("CONSTRAINT")) {
						dir.setType(DirectiveType.CONSTRAINT);
					} else if (preProcessed.startsWith("MONITORING")) {
						dir.setType(DirectiveType.MONITORING);
					} else {
						throw new ComotIllegalArgumentException("Can not determine the type of the  SYBL directive: '"
								+ dir.getDirective() + "'");
					}
				}
			}
		}

		coordinator.reconfigureElasticity(serviceId, instanceId, service);

		return Response.ok().build();
	}

	@GET
	@Path("/services/{serviceId}/instances/{instanceId}/events")
	@Consumes(SseFeature.SERVER_SENT_EVENTS)
	@Produces(SseFeature.SERVER_SENT_EVENTS)
	public EventOutput getServerSentEvents(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId) throws Exception {

		final EventOutput eventOutput = new EventOutput();

		UiAdapter processor = context.getBean(UiAdapter.class);
		processor.setUiAdapter(instanceId, eventOutput);
		processor.checkClient();

		Manager manager = context.getBean(SingleQueueManager.class);
		manager.start("UI_" + UUID.randomUUID().toString(), processor);

		return eventOutput;
	}

	@PUT
	@Path("/eps/{epsId}/instances")
	@Produces(MediaType.TEXT_PLAIN)
	public Response createDynamicEps(
			@PathParam("epsId") String epsId) throws EpsException, ComotException, ClassNotFoundException,
			IOException, JAXBException {

		String serviceInstanceId = coordinator.createDynamicService(epsId);
		return Response.ok(serviceInstanceId).build();
	}

	@DELETE
	@Path("/eps/{epsId}/instances/{epsInstanceId}")
	public Response removeDynamicEps(
			@PathParam("epsInstanceId") String epsInstanceId,
			@PathParam("epsId") String epsId) throws EpsException, ComotException, ClassNotFoundException,
			IOException, JAXBException {

		coordinator.removeDynamicService(epsId, epsInstanceId);
		return Response.ok().build();
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

		for (LifeCycleTransition tr : lifeCycle.getTransitions()) {
			lc.addTransition(tr.getState(), tr.getAction(), tr.getNextState());
		}

		return Response.ok(lc).build();
	}

	/**
	 * 
	 * @param type
	 *            NON_EPS | EPS | ALL
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws EpsException
	 */
	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/services")
	public Response getServices(
			@DefaultValue(InformationClient.NON_EPS) @QueryParam("type") String type) throws ClassNotFoundException, IOException,
			EpsException {

		type = type.toUpperCase();

		List<CloudService> allServices = infoServ.getServices();

		Set<String> dynamicEpsServices = new HashSet<String>();
		for (OfferedServiceUnit osu : infoServ.getOsus()) {
			if (InformationClient.isDynamicEps(osu)) {
				dynamicEpsServices.add(osu.getService().getId());
			}
		}

		if (InformationClient.ALL.equals(type)) {

		} else if (InformationClient.EPS.equals(type)) {

			for (Iterator<CloudService> iterator = allServices.iterator(); iterator.hasNext();) {
				CloudService service = iterator.next();
				if (!dynamicEpsServices.contains(service.getId())) {
					iterator.remove();
				}
			}

		} else if (InformationClient.NON_EPS.equals(type)) {

			for (Iterator<CloudService> iterator = allServices.iterator(); iterator.hasNext();) {
				CloudService service = iterator.next();
				if (dynamicEpsServices.contains(service.getId())) {
					iterator.remove();
				}
			}

		} else {
			allServices = new ArrayList<CloudService>();
		}

		return Response.ok(allServices.toArray(new CloudService[allServices.size()])).build();
	}

	/**
	 * 
	 * @param type
	 *            ALL | DYNAMIC | STATIC
	 * @return
	 * @throws EpsException
	 */
	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/eps")
	public Response getElasticPlatformServices(
			@DefaultValue(InformationClient.ALL) @QueryParam("type") String type) throws EpsException {

		List<OfferedServiceUnit> allEps = new ArrayList<>(infoServ.getOsus());

		if (InformationClient.ALL.equals(type)) {

		} else if (InformationClient.STATIC.equals(type)) {
			for (Iterator<OfferedServiceUnit> iterator = allEps.iterator(); iterator.hasNext();) {
				OfferedServiceUnit osu = iterator.next();
				if (InformationClient.isDynamicEps(osu)) {
					iterator.remove();
				}
			}

		} else if (InformationClient.DYNAMIC.equals(type)) {
			for (Iterator<OfferedServiceUnit> iterator = allEps.iterator(); iterator.hasNext();) {
				OfferedServiceUnit osu = iterator.next();
				if (!InformationClient.isDynamicEps(osu)) {
					iterator.remove();
				}
			}

		} else {
			allEps = new ArrayList<OfferedServiceUnit>();
		}

		return Response.ok(allEps.toArray(new OfferedServiceUnit[allEps.size()])).build();
	}

	/**
	 * 
	 * @param type
	 *            ALL | DYNAMIC | STATIC
	 * @return
	 * @throws EpsException
	 */
	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/eps/instances")
	public Response getElasticPlatformServicesInstances(
			@DefaultValue(InformationClient.ALL) @QueryParam("type") String type) throws EpsException {

		List<OsuInstance> allEpsInstances = infoServ.getElasticPlatformServicesInstances(type);

		return Response.ok(allEpsInstances.toArray(new OsuInstance[allEpsInstances.size()])).build();
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/services/{serviceId}/instances/{instanceId}")
	public Response getServicesInstance(
			@PathParam("serviceId") String serviceId,
			@PathParam("instanceId") String instanceId) throws ClassNotFoundException, IOException, EpsException {

		ServiceInstanceUi instanceUi = new ServiceInstanceUi(
				instanceId,
				infoServ.getServiceInstance(serviceId, instanceId),
				lcManager.getCurrentState(instanceId));

		return Response.ok(instanceUi).build();
	}

	// GUI only

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/services/allInstances")
	public Response getAllInstances() throws EpsException {

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

}
