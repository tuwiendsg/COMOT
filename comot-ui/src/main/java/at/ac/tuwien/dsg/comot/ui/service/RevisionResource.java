package at.ac.tuwien.dsg.comot.ui.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.ac.tuwien.dsg.comot.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;
import at.ac.tuwien.dsg.comot.common.coreservices.MonitoringClient;
import at.ac.tuwien.dsg.comot.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.servrec.RecordingManager;

// WADL http://localhost:8380/comot/rest/application.wadl
@Service
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Path("/services/{serviceId}/revisions")
public class RevisionResource {

	private static final Logger log = LoggerFactory.getLogger(RevisionResource.class);

	@Autowired
	protected RecordingManager recordingManager;

	@Autowired
	protected DeploymentClient deployment;
	@Autowired
	protected ControlClient control;
	@Autowired
	protected MonitoringClient monitoring;

	@PUT
	@Path("/recording")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response startOrRestartRecording(@PathParam("serviceId") String serviceId) throws CoreServiceException,
			ComotException {

		recordingManager.addService(serviceId, deployment, control, monitoring);
		recordingManager.startRecording(serviceId);

		return Response.ok().build();
	}

	@DELETE
	@Path("/recording")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response stopRecording(@PathParam("serviceId") String serviceId) throws CoreServiceException, ComotException {

		recordingManager.stopRecording(serviceId);

		return Response.ok().build();
	}

	@DELETE
	@Path("/")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteDataAndStopRecording(@PathParam("serviceId") String serviceId) throws CoreServiceException,
			ComotException {

		recordingManager.removeService(serviceId);

		return Response.ok().build();
	}

	@GET
	@Path("/last")
	@Consumes(MediaType.WILDCARD)
	public Response getLastRevision(@PathParam("serviceId") String serviceId) throws CoreServiceException,
			ComotException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			ClassNotFoundException, RecorderException {

		log.info("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");

		CloudService service = recordingManager.getRevision(serviceId, Long.MAX_VALUE);

		return Response.ok(service).build();
	}

}
