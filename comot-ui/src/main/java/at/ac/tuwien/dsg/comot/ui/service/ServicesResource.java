package at.ac.tuwien.dsg.comot.ui.service;

import java.util.List;

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

import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.monitoring.ElementMonitoring;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.core.ComotOrchestrator;
import at.ac.tuwien.dsg.comot.core.model.ServiceEntity;
import at.ac.tuwien.dsg.comot.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.ui.mapper.SalsaOutputMapper;
import at.ac.tuwien.dsg.comot.ui.model.ElementState;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

// WADL http://localhost:8380/comot/rest/application.wadl
@Service
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Path("/services")
public class ServicesResource {

	private static final Logger log = LoggerFactory.getLogger(ServicesResource.class);

	@Autowired
	protected SalsaOutputMapper mapperOutput;
	@Autowired
	protected ToscaMapper mapperTosca;

	@Autowired
	protected ComotOrchestrator orchestrator;

	@PostConstruct
	public void startUp() {
		log.info("REST resource created");
	}

	// CREATE & UPDATE

	@POST
	@Path("/")
	@Produces(MediaType.TEXT_PLAIN)
	public Response createAndDeploy(Definitions def) {

		try {

			orchestrator.deployNew(mapperTosca.createModel(def));
			return Response.ok().build();

		} catch (Exception e) {
			return handleException(e);
		}
	}

	@PUT
	@Path("/{serviceId}/monitoring")
	@Produces(MediaType.TEXT_PLAIN)
	public Response startMonitoring(@PathParam("serviceId") String serviceId) {

		try {
			orchestrator.startMonitoring(serviceId);
			return Response.ok().build();

		} catch (Exception e) {
			return handleException(e);
		}
	}

	@PUT
	@Path("/{serviceId}/control")
	@Produces(MediaType.TEXT_PLAIN)
	public Response startControl(@PathParam("serviceId") String serviceId) {

		try {
			orchestrator.startControl(serviceId);
			return Response.ok().build();

		} catch (Exception e) {
			return handleException(e);
		}
	}

	@PUT
	@Path("/{serviceId}/mcr")
	@Produces(MediaType.TEXT_PLAIN)
	public Response createMcr(@PathParam("serviceId") String serviceId, CompositionRulesConfiguration mcr) {

		try {
			orchestrator.setMcr(serviceId, mcr);
			return Response.ok().build();

		} catch (Exception e) {
			return handleException(e);
		}
	}

	@PUT
	@Path("/{serviceId}/effects")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createElasticityEffects(@PathParam("serviceId") String serviceId, String input) {

		try {
			orchestrator.startMonitoring(serviceId);// todo
			return Response.ok().build();

		} catch (Exception e) {
			return handleException(e);
		}
	}

	// update structure / requirements for monitoring / SYBL directives
	@PUT
	@Path("/{serviceId}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response updateService(@PathParam("serviceId") String serviceId, Definitions def) {

		try {
			// TODO
			return null;
		} catch (Exception e) {
			return handleException(e);
		}
	}

	// DELETE

	@DELETE
	@Path("/{serviceId}/monitoring")
	@Produces(MediaType.TEXT_PLAIN)
	public Response stopMonitoring(@PathParam("serviceId") String serviceId) {

		try {
			orchestrator.stopMonitoring(serviceId);
			return Response.ok().build();

		} catch (Exception e) {
			return handleException(e);
		}
	}

	@DELETE
	@Path("/{serviceId}/control")
	@Produces(MediaType.TEXT_PLAIN)
	public Response stopControl(@PathParam("serviceId") String serviceId) {

		try {
			orchestrator.stopControl(serviceId);
			return Response.ok().build();

		} catch (Exception e) {
			return handleException(e);
		}
	}

	// READ

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/")
	public Response getServices() {

		try {
			List<ServiceEntity> list = orchestrator.getServices();

			return Response.ok(list.toArray(new ServiceEntity[list.size()])).build();
		} catch (Exception e) {
			return handleException(e);
		}
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/{serviceId}")
	public Response getService(@PathParam("serviceId") String serviceId) {

		try {
			CloudService service = orchestrator.getStatus(serviceId);
			ElementState element = mapperOutput.extractOutput(service);

			return Response.ok(element).build();

		} catch (Exception e) {
			return handleException(e);
		}
	}

	// return status from salsa & monitoring (depends on what is turned on)
	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/{serviceId}/state")
	public Response getState(@PathParam("serviceId") String serviceId) {

		try {
			CloudService service = orchestrator.getStatus(serviceId);
			ElementState element = mapperOutput.extractOutput(service);

			return Response.ok(element).build();

		} catch (Exception e) {
			return handleException(e);
		}
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/{serviceId}/monitoring/snapshots/last")
	public Response getMonitoringData(@PathParam("serviceId") String serviceId) {

		try {
			ElementMonitoring element = orchestrator.getMonitoringData(serviceId);

			return Response.ok(element).build();

		} catch (Exception e) {
			return handleException(e);
		}
	}

	protected Response handleException(Exception e) {

		if (e.getClass().equals(ComotIllegalArgumentException.class)) {
			log.warn("Wrong user input: {}", e.getMessage());
			return Response.status(404).entity(e.getMessage()).build();

		} else if (e.getClass().equals(CoreServiceException.class)) {
			CoreServiceException ce = (CoreServiceException) e;

			if (ce.isClientError()) {
				log.warn("Core service CLIENT ERROR - {}", e);
			} else {
				log.warn("Core service SERVER ERROR - {}", e);
			}
			return Response.status(ce.getCode()).entity(ce.getMsg()).build();

		} else if (e.getClass().equals(ComotException.class)) {
			log.error("Something bad happened: {}", e);
			return Response.serverError().build();

		} else {
			log.error("Wut? {}", e);
			return Response.serverError().build();
		}

	}

}
