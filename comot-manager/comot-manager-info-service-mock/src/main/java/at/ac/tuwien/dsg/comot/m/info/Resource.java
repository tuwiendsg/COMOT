package at.ac.tuwien.dsg.comot.m.info;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.Template;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;

@Service
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Path("/")
public class Resource {

	private static final Logger log = LoggerFactory.getLogger(Resource.class);

	@Autowired
	protected ApplicationContext context;
	@Autowired
	protected InformationServiceMock infoServ;

	protected Object sync = new Object();

	@POST
	@Path(Constants.TEMPLATES)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createTemplate(CloudService service) throws EpsException, ComotException, JAXBException,
			ClassNotFoundException, IOException {
		synchronized (sync) {
			infoServ.createTemplate(service.getId(), service);
		}
		return Response.ok().build();
	}

	@DELETE
	@Path(Constants.TEMPLATES_ONE)
	public Response deleteTemplate(@PathParam("templateId") String templateId) throws EpsException,
			ComotException {
		synchronized (sync) {
			infoServ.removeTemplate(templateId);
		}
		return Response.ok().build();
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path(Constants.TEMPLATES)
	public Response getTemplates() throws ClassNotFoundException, IOException, EpsException {

		List<Template> templates;
		synchronized (sync) {
			templates = infoServ.getTemplates();
		}
		return Response.ok(templates.toArray(new Template[templates.size()])).build();
	}

	// SERVICE

	@POST
	@Path(Constants.TEMPLATES_ONE_SERVICES)
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createServiceFromTemplate(
			@PathParam("templateId") String templateId) throws EpsException, ComotException, JAXBException,
			ClassNotFoundException, IOException {
		String result;
		synchronized (sync) {
			result = infoServ.createServiceFromTemplate(templateId);
		}
		return Response.ok(result).build();
	}

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Path(Constants.SERVICES)
	public Response createService(CloudService service) throws ClassNotFoundException, IOException {
		String result;
		synchronized (sync) {

			result = infoServ.createService(service);
		}
		return Response.ok(result).build();
	}

	@DELETE
	@Path(Constants.SERVICE_ONE)
	public Response deleteService(
			@PathParam("serviceId") String serviceId) {
		synchronized (sync) {
			infoServ.removeService(serviceId);
		}
		return Response.ok().build();
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path(Constants.SERVICES)
	public Response getServices() {
		List<CloudService> result;
		synchronized (sync) {
			result = infoServ.getServices();
		}
		final GenericEntity<List<CloudService>> list = new GenericEntity<List<CloudService>>(result) {
		};

		return Response.ok(list).build();
	}

	@PUT
	@Produces(MediaType.TEXT_PLAIN)
	@Path(Constants.SERVICE_ONE_ELASTICITY)
	public Response reconfigureElasticity(@PathParam("serviceId") String serviceId, CloudService service)
			throws ClassNotFoundException, IOException {

		synchronized (sync) {

			infoServ.reconfigureElasticity(serviceId, service);
		}
		return Response.ok().build();
	}

	// UNIT INSTANCES
	@PUT
	@Path(Constants.UNIT_INSTANCE_ONE)
	public Response putUnitInstance(
			@PathParam("serviceId") String serviceId,
			@PathParam("unitId") String unitId,
			UnitInstance uInst) {
		synchronized (sync) {
			infoServ.putUnitInstance(serviceId, unitId, uInst);
		}
		return Response.ok().build();
	}

	@DELETE
	@Consumes(MediaType.WILDCARD)
	@Path(Constants.UNIT_INSTANCE_ONE)
	public Response removeUnitInstance(
			@PathParam("serviceId") String serviceId,
			@PathParam("unitInstanceId") String unitInstanceId) {
		synchronized (sync) {
			infoServ.removeUnitInstance(serviceId, unitInstanceId);
		}
		return Response.ok().build();
	}

	// OSU

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path(Constants.EPSES)
	public Response getOsus() {
		List<OfferedServiceUnit> result;
		synchronized (sync) {
			result = infoServ.getOsus();
		}
		final GenericEntity<List<OfferedServiceUnit>> list = new GenericEntity<List<OfferedServiceUnit>>(result) {
		};

		return Response.ok(list).build();
	}

	@POST
	@Path(Constants.EPSES)
	public Response addOsu(OfferedServiceUnit osu) {
		synchronized (sync) {
			infoServ.addOsu(osu);
		}
		return Response.ok().build();
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path(Constants.EPS_INSTANCES_ALL)
	public Response getOsuInstances() {
		List<OsuInstance> result;
		synchronized (sync) {
			result = infoServ.getOsusInstances();

			log.info("getOsuInstances() {}", result);
		}
		final GenericEntity<List<OsuInstance>> list = new GenericEntity<List<OsuInstance>>(result) {
		};

		return Response.ok(list).build();
	}

	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Path(Constants.EPS_ONE_INSTANCES)
	public Response createOsuInstance(
			@PathParam("epsId") String epsId) throws ClassNotFoundException, IOException {
		String result;
		synchronized (sync) {
			result = infoServ.createOsuInstance(epsId);
		}
		return Response.ok(result).build();
	}

	@DELETE
	@Consumes(MediaType.WILDCARD)
	@Path(Constants.EPS_INSTANCE_ONE)
	public Response removeOsuInatance(
			@PathParam("epsInstanceId") String epsInstanceId) {
		synchronized (sync) {
			infoServ.removeOsuInatance(epsInstanceId);
		}
		return Response.ok().build();
	}

	@PUT
	@Consumes(MediaType.WILDCARD)
	@Path(Constants.EPS_INSTANCE_ASSIGNMENT)
	public Response assignEps(
			@PathParam("serviceId") String serviceId,
			@PathParam("epsId") String epsId) {
		synchronized (sync) {
			infoServ.assignSupportingService(serviceId, epsId);
		}
		return Response.ok().build();
	}

	@DELETE
	@Consumes(MediaType.WILDCARD)
	@Path(Constants.EPS_INSTANCE_ASSIGNMENT)
	public Response removeEpsAssignment(
			@PathParam("serviceId") String instanceId,
			@PathParam("epsId") String epsId) {
		synchronized (sync) {
			infoServ.removeAssignmentOfSupportingOsu(instanceId, epsId);
		}
		return Response.ok().build();
	}

	@DELETE
	@Consumes(MediaType.WILDCARD)
	@Path(Constants.DELETE_ALL)
	public Response deleteAll() {
		synchronized (sync) {
			infoServ.deleteAll();
		}
		return Response.ok().build();
	}

}
