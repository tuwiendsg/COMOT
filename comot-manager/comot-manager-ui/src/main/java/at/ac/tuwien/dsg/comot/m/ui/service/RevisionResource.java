package at.ac.tuwien.dsg.comot.m.ui.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.m.core.lifecycle.adapters.RecordingAdapter;
import at.ac.tuwien.dsg.comot.m.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.m.recorder.model.Change;
import at.ac.tuwien.dsg.comot.m.recorder.out.ManagedObject;

// WADL http://localhost:8380/comot/rest/application.wadl
@Service
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Path("/recordings/{instanceId}")
public class RevisionResource {

	private static final Logger log = LoggerFactory.getLogger(RevisionResource.class);

	@Autowired
	protected RecordingAdapter recorder;

	// @GET
	// @Path("/last")
	// @Consumes(MediaType.WILDCARD)
	// public Response getLastRevision(@PathParam("instanceId") String instanceId) throws CoreServiceException,
	// ComotException, InstantiationException, IllegalAccessException, IllegalArgumentException,
	// ClassNotFoundException, RecorderException {
	// // TODO wrong !! object id shoud be stringId
	// CloudService service = (CloudService) recorder.getRevision(instanceId, instanceId, Long.MAX_VALUE);
	//
	// return Response.ok(service).build();
	// }

	@GET
	@Path("/objects")
	@Consumes(MediaType.WILDCARD)
	public Response getManagedObjects(
			@PathParam("instanceId") String instanceId
			) throws CoreServiceException,
					ComotException, InstantiationException, IllegalAccessException, IllegalArgumentException,
					ClassNotFoundException, RecorderException {

		List<ManagedObject> objects = recorder.getManagedObjects(instanceId);
		final GenericEntity<List<ManagedObject>> list = new GenericEntity<List<ManagedObject>>(objects) {
		};
		// JaxbList<String> entity = new JaxbList<String>(ids);
		return Response.ok(list).build();
	}

	@GET
	@Path("/objects/{objectId}/{timestamp}")
	@Consumes(MediaType.WILDCARD)
	public Response getRecording(
			@PathParam("instanceId") String instanceId,
			@PathParam("objectId") String objectId,
			@PathParam("timestamp") Long timestamp) throws CoreServiceException,
			ComotException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			ClassNotFoundException, RecorderException {

		log.info("getRevision(serviceId={}, objectId={}, timestamp={})", instanceId, objectId, timestamp);

		Object obj = recorder.getRevision(instanceId, objectId, timestamp);

		return Response.ok(obj).build();
	}

	@GET
	@Path("/objects/{objectId}/events")
	@Consumes(MediaType.WILDCARD)
	public Response getEvents(
			@PathParam("instanceId") String instanceId,
			@PathParam("objectId") String objectId,
			@DefaultValue("0") @QueryParam("from") Long from,
			@DefaultValue("9223372036854775807") @QueryParam("to") Long to) // def Long.MAX_VALUE
			throws CoreServiceException, ComotException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, ClassNotFoundException, RecorderException, JAXBException {

		log.info("getChanges(serviceId={}, objectId={}, from={}, to={})", instanceId, objectId, from, to);

		List<Change> change = recorder.getAllChanges(instanceId, objectId, from, to);

		// log.info("{}", Utils.asXmlString(change));

		final GenericEntity<List<Change>> entity = new GenericEntity<List<Change>>(change) {
		};
		return Response.ok(entity).build();
	}

}
