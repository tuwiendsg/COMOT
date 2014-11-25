package at.ac.tuwien.dsg.comot.ui.service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
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

import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.core.ComotOrchestrator;
import at.ac.tuwien.dsg.comot.core.model.ServiceEntity;
import at.ac.tuwien.dsg.comot.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.ui.mapper.OutputMapper;
import at.ac.tuwien.dsg.comot.ui.model.Element;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

// WADL http://localhost:8380/comot/rest/application.wadl
@Service
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Path("/services")
public class ServicesResource {

	private static final Logger log = LoggerFactory.getLogger(ServicesResource.class);

	@Autowired
	protected DeploymentClient deploy;
	@Autowired
	protected OutputMapper mapperOutput;
	@Autowired
	protected ToscaMapper mapperTosca;

	@Autowired
	protected ComotOrchestrator orchestrator;

	@PostConstruct
	public void startUp() {
		log.info("REST resource created");
	}

	// CREATE

	@POST
	@Path("/")
	public Response createAndDeploy(Definitions def) {

		try {

			orchestrator.deployNew(mapperTosca.createModel(def));
			return Response.ok().build();

		} catch (CoreServiceException | ComotException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}

	}

	@POST
	@Path("/{serviceId}/monitoring")
	public Response startMonitoring(
			@PathParam("serviceId") String serviceId,
			CompositionRulesConfiguration mcr) {

		try {
			orchestrator.startMonitoring(serviceId, mcr);
			return Response.ok().build();

		} catch (CoreServiceException | ComotException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{serviceId}/control")
	public Response startControl(
			@PathParam("serviceId") String serviceId,
			String input) { // this must hold MCR as well as effects

		try {
			orchestrator.startControl(serviceId, null, null); // TODO extract MCR and efects
			return Response.ok().build();

		} catch (CoreServiceException | ComotException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}

	}

	// UPDATE

	// update structure / requirements for monitoring / SYBL directives
	@PUT
	@Path("/{serviceId}")
	public Response updateService(
			@PathParam("serviceId") String serviceId,
			Definitions def) {

		return null; // TODO
	}

	@PUT
	@Path("/{serviceId}/monitoring/mcr")
	public Response updateMcrForMonitoring(
			@PathParam("serviceId") String serviceId,
			CompositionRulesConfiguration mcr) {

		return null; // TODO
	}

	@PUT
	@Path("/{serviceId}/control/mcr")
	public Response updateMcrForControl(
			@PathParam("serviceId") String serviceId,
			CompositionRulesConfiguration mcr) {

		return null; // TODO
	}

	@PUT
	@Path("/{serviceId}/control/effects")
	public Response updateEffects(
			@PathParam("serviceId") String serviceId,
			String effects) {

		return null; // TODO
	}

	// READ

	@GET
	@Path("/")
	public Response getServices() {

		List<ServiceEntity> list = orchestrator.getServices();

		return Response.ok(list.toArray(new ServiceEntity[list.size()])).build();
	}

	@GET
	@Path("/{serviceId}")
	public Response getService() {

		return null; // TODO
	}

	@GET
	@Path("/{serviceId}/tosca_original")
	public Response getToscaOriginal(@PathParam("serviceId") String serviceId) {

		return null; // TODO
	}

	@GET
	@Path("/{serviceId}/tosca_deployed")
	public Response getToscaDeployed(@PathParam("serviceId") String serviceId) {

		return null; // TODO
	}

	// return status from salsa & monitoring (depends on what is turned on)
	@GET
	@Path("/{serviceId}/status")
	public Response getStatus(@PathParam("serviceId") String serviceId) {

		try {
			CloudService service = orchestrator.getStatus(serviceId);
			Element element = mapperOutput.extractOutput(service);

			return Response.ok(element).build();

		} catch (CoreServiceException | ComotException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

}
