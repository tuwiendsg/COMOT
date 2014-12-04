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

	// public static final String TOSCA_FILE = "./TOSCA-v1.0.xsd";

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
	public Response createAndDeploy(Definitions def) throws CoreServiceException, ComotException {

		orchestrator.deployNew(mapperTosca.createModel(def));
		return Response.ok(def.getId()).build();
	}

	@PUT
	@Path("/{serviceId}/deployment")
	@Produces(MediaType.TEXT_PLAIN)
	public Response deploy(@PathParam("serviceId") String serviceId) throws CoreServiceException, ComotException {

		orchestrator.deploy(serviceId);
		return Response.ok().build();
	}

	@PUT
	@Path("/{serviceId}/monitoring")
	@Produces(MediaType.TEXT_PLAIN)
	public Response startMonitoring(@PathParam("serviceId") String serviceId) throws CoreServiceException,
			ComotException {

		orchestrator.startMonitoring(serviceId);
		return Response.ok().build();
	}

	@PUT
	@Path("/{serviceId}/control")
	@Produces(MediaType.TEXT_PLAIN)
	public Response startControl(@PathParam("serviceId") String serviceId) throws CoreServiceException, ComotException {

		orchestrator.startControl(serviceId);
		return Response.ok().build();
	}

	@PUT
	@Path("/{serviceId}/mcr")
	public Response createMcr(@PathParam("serviceId") String serviceId, CompositionRulesConfiguration mcr)
			throws CoreServiceException, ComotException {

		orchestrator.setMcr(serviceId, mcr);
		return Response.ok().build();
	}

	@PUT
	@Path("/{serviceId}/effects")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createElasticityEffects(@PathParam("serviceId") String serviceId, String input)
			throws CoreServiceException, ComotException {

		orchestrator.startMonitoring(serviceId);// todo
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
	@Path("/{serviceId}/deployment")
	public Response undeploy(@PathParam("serviceId") String serviceId) throws CoreServiceException,
			ComotException {

		orchestrator.undeploy(serviceId);
		return Response.ok().build();
	}

	@DELETE
	@Path("/{serviceId}/monitoring")
	public Response stopMonitoring(@PathParam("serviceId") String serviceId) throws CoreServiceException,
			ComotException {

		orchestrator.stopMonitoring(serviceId);
		return Response.ok().build();
	}

	@DELETE
	@Path("/{serviceId}/control")
	public Response stopControl(@PathParam("serviceId") String serviceId) throws CoreServiceException, ComotException {

		orchestrator.stopControl(serviceId);
		return Response.ok().build();
	}

	// READ

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/")
	public Response getServices() {

		List<ServiceEntity> list = orchestrator.getServices();
		return Response.ok(list.toArray(new ServiceEntity[list.size()])).build();
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/{serviceId}")
	public Response getService(@PathParam("serviceId") String serviceId) throws CoreServiceException, ComotException {

		CloudService service = orchestrator.getStatus(serviceId);
		ElementState element = mapperOutput.extractOutput(service);
		return Response.ok(element).build();
	}

	// return status from salsa & monitoring (depends on what is turned on)
	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/{serviceId}/state")
	public Response getState(@PathParam("serviceId") String serviceId) throws CoreServiceException, ComotException {

		CloudService service = orchestrator.getStatus(serviceId);
		ElementState element = mapperOutput.extractOutput(service);
		return Response.ok(element).build();
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/{serviceId}/monitoring/snapshots/last")
	public Response getMonitoringData(@PathParam("serviceId") String serviceId) throws CoreServiceException,
			ComotException {

		ElementMonitoring element = orchestrator.getMonitoringData(serviceId);
		return Response.ok(element).build();
	}

	// @GET
	// @Consumes(MediaType.WILDCARD)
	// @Produces(MediaType.APPLICATION_XML)
	// @Path("/tosca")
	// public Response getToscaXsd() {
	//
	// try {
	// return Response.ok(IOUtils.toString(ClassLoader.getSystemResourceAsStream(TOSCA_FILE), "UTF-8")).build();
	//
	// } catch (Exception e) {
	// return handleException(e);
	// }
	// }

}
