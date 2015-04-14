package at.ac.tuwien.dsg.comot.m.ui.service;

import java.util.ArrayList;
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

import at.ac.tuwien.dsg.comot.m.common.exception.ComotIllegalArgumentException;
import at.ac.tuwien.dsg.comot.m.core.analytics.ElasticPlanReport;
import at.ac.tuwien.dsg.comot.m.core.analytics.ElasticityAnalyzis;
import at.ac.tuwien.dsg.comot.m.core.analytics.ResultLine;
import at.ac.tuwien.dsg.comot.m.core.analytics.TimeAnalyzis;
import at.ac.tuwien.dsg.comot.m.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.m.recorder.model.Change;
import at.ac.tuwien.dsg.comot.m.recorder.out.ManagedObject;
import at.ac.tuwien.dsg.comot.m.recorder.revisions.RevisionApi;

// WADL http://localhost:8380/comot/rest/application.wadl
@Service
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Path("/recordings/{instanceId}")
public class RevisionResource {

	private static final Logger log = LoggerFactory.getLogger(RevisionResource.class);

	@Autowired
	protected RevisionApi revisionApi;
	@Autowired
	protected TimeAnalyzis analyticEngine;
	@Autowired
	protected ElasticityAnalyzis elAnalysis;

	@GET
	@Path("/{serviceId}/analytics/unitInstanceDeploymentEvents")
	@Consumes(MediaType.WILDCARD)
	public Response getUnitInstanceDeploymentEvents(
			@PathParam("instanceId") String instanceId,
			@PathParam("serviceId") String serviceId
			) throws InstantiationException, IllegalAccessException, IllegalArgumentException, ClassNotFoundException,
					RecorderException {

		List<ResultLine> results = analyticEngine.deploymentEvents(serviceId, instanceId);

		final GenericEntity<List<ResultLine>> list = new GenericEntity<List<ResultLine>>(results) {
		};
		return Response.ok(list).build();
	}

	@GET
	@Path("/{serviceId}/analytics/elasticActions")
	@Consumes(MediaType.WILDCARD)
	public Response getElasticActions(
			@PathParam("instanceId") String instanceId,
			@PathParam("serviceId") String serviceId
			) throws InstantiationException, IllegalAccessException, IllegalArgumentException, ClassNotFoundException,
					JAXBException, RecorderException {

		List<ElasticPlanReport> results = elAnalysis.doOneService(serviceId, instanceId);

		final GenericEntity<List<ElasticPlanReport>> list = new GenericEntity<List<ElasticPlanReport>>(results) {
		};
		return Response.ok(list).build();
	}

	@GET
	@Path("/objects")
	@Consumes(MediaType.WILDCARD)
	public Response getManagedObjects(
			@PathParam("instanceId") String instanceId
			) {

		List<ManagedObject> objects = revisionApi.getManagedObjects(instanceId);
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
			@PathParam("timestamp") Long timestamp) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, ClassNotFoundException, RecorderException {

		log.info("getRevision(serviceId={}, objectId={}, timestamp={})", instanceId, objectId, timestamp);

		if (!revisionApi.verifyObject(instanceId, objectId)) {
			throw new ComotIllegalArgumentException("For service " + instanceId + " there is no managed object "
					+ objectId);
		}

		Object obj = revisionApi.getRevision(instanceId, objectId, timestamp);

		if (obj == null) {
			throw new ComotIllegalArgumentException("There is no revision of service" + instanceId + ", object="
					+ objectId + " at time=" + timestamp + " ");
		}

		return Response.ok(obj).build();
	}

	@GET
	@Path("/events")
	@Consumes(MediaType.WILDCARD)
	public Response getAllEventsInRange(
			@PathParam("instanceId") String instanceId,
			@DefaultValue("0") @QueryParam("from") Long from,
			@DefaultValue("9223372036854775807") @QueryParam("to") Long to) // def Long.MAX_VALUE
	{

		Change change = revisionApi.getAllChanges(instanceId, from, to);

		List<Change> list = new ArrayList<>();
		while (change != null) {
			list.add(change);
			change = change.getTo().getEnd();
		}

		final GenericEntity<List<Change>> entity = new GenericEntity<List<Change>>(list) {
		};
		return Response.ok(entity).build();
	}

	@GET
	@Path("/objects/{objectId}/events")
	@Consumes(MediaType.WILDCARD)
	public Response getEvents(
			@PathParam("instanceId") String instanceId,
			@PathParam("objectId") String objectId,
			@DefaultValue("0") @QueryParam("from") Long from,
			@DefaultValue("9223372036854775807") @QueryParam("to") Long to) // def Long.MAX_VALUE
	{

		if (!revisionApi.verifyObject(instanceId, objectId)) {
			throw new ComotIllegalArgumentException("For service " + instanceId + " there is no managed object "
					+ objectId);
		}

		Change change = revisionApi.getAllChangesThatModifiedThisObject(instanceId, objectId, from, to);

		List<Change> list = new ArrayList<>();
		while (change != null) {
			list.add(change);
			change = change.getTo().getEnd();
		}

		final GenericEntity<List<Change>> entity = new GenericEntity<List<Change>>(list) {
		};
		return Response.ok(entity).build();
	}

}
