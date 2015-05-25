/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
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

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

// WADL http://localhost:8380/comot/rest/application.wadl
@Service
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Path("/recordings/{serviceId}")
@Api(value = "/recordings", description = "Lifecycle recorder")
public class RevisionResource {

	private static final Logger LOG = LoggerFactory.getLogger(RevisionResource.class);

	@Autowired
	protected RevisionApi revisionApi;
	@Autowired
	protected TimeAnalyzis analyticEngine;
	@Autowired
	protected ElasticityAnalyzis elAnalysis;

	@GET
	@Path("/analytics/unitInstanceDeploymentEvents")
	@Consumes(MediaType.WILDCARD)
	@ApiOperation(
			value = "Get deployment times of unit instances",
			notes = "Returns multiple entries for each instance of all service units. An entry is associated to either a certain stage of the deployment or summarizes the entire deployment.",
			response = ResultLine.class,
			responseContainer = "List")
	public Response getUnitInstanceDeploymentEvents(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String serviceId)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, ClassNotFoundException,
			RecorderException {

		List<ResultLine> results = analyticEngine.deploymentEvents(serviceId);

		final GenericEntity<List<ResultLine>> list = new GenericEntity<List<ResultLine>>(results) {
		};
		return Response.ok(list).build();
	}

	@GET
	@Path("/analytics/elasticActions")
	@Consumes(MediaType.WILDCARD)
	@ApiOperation(
			value = "Get elastic changes and their effects",
			notes = "One entry summarizes all actions performed and effected service unit instances",
			response = ElasticPlanReport.class,
			responseContainer = "List")
	public Response getElasticActions(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String serviceId)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, ClassNotFoundException,
			JAXBException, RecorderException {

		List<ElasticPlanReport> results = elAnalysis.doOneService(serviceId);

		final GenericEntity<List<ElasticPlanReport>> list = new GenericEntity<List<ElasticPlanReport>>(results) {
		};
		return Response.ok(list).build();
	}

	@GET
	@Path("/objects")
	@Consumes(MediaType.WILDCARD)
	@ApiOperation(
			value = "Get all versioned parts of the cloud service",
			response = ManagedObject.class,
			responseContainer = "List")
	public Response getManagedObjects(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String instanceId) {

		List<ManagedObject> objects = revisionApi.getManagedObjects(instanceId);
		final GenericEntity<List<ManagedObject>> list = new GenericEntity<List<ManagedObject>>(objects) {
		};

		return Response.ok(list).build();
	}

	@GET
	@Path("/objects/{objectId}/{timestamp}")
	@Consumes(MediaType.WILDCARD)
	@ApiOperation(
			value = "Get a version of the object valid at certain time",
			notes = "Cloud service is managed as an object composed from other nested complex objects. This operation allows to retrieve a state of the cloud service or of any of its parts.")
	public Response getRecording(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String serviceId,
			@ApiParam(value = "ID of the managed object", required = true) @PathParam("objectId") String objectId,
			@ApiParam(value = "Point in time, for which to retrieve the state", required = true) @PathParam("timestamp") Long timestamp)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, ClassNotFoundException,
			RecorderException {

		LOG.info("getRevision(serviceId={}, objectId={}, timestamp={})", serviceId, objectId, timestamp);

		if (!revisionApi.verifyObject(serviceId, objectId)) {
			throw new ComotIllegalArgumentException("For service " + serviceId + " there is no managed object "
					+ objectId);
		}

		Object obj = revisionApi.getRevision(serviceId, objectId, timestamp);

		if (obj == null) {
			throw new ComotIllegalArgumentException("There is no revision of service" + serviceId + ", object="
					+ objectId + " at time=" + timestamp + " ");
		}

		return Response.ok(obj).build();
	}

	@GET
	@Path("/events")
	@Consumes(MediaType.WILDCARD)
	@ApiOperation(
			value = "Get events in a range",
			response = Change.class,
			responseContainer = "List")
	public Response getAllEventsInRange(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String serviceId,
			@ApiParam(value = "Lower bound", required = false) @DefaultValue("0") @QueryParam("from") Long from,
			@ApiParam(value = "Upper bound", required = false) @DefaultValue("9223372036854775807") @QueryParam("to") Long to) { // def
		// Long.MAX_VALUE

		Change change = revisionApi.getAllChanges(serviceId, from, to);

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
	@ApiOperation(
			value = "Get events in range that effected the object",
			response = Change.class,
			responseContainer = "List")
	public Response getEvents(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String serviceId,
			@ApiParam(value = "ID of the managed object", required = true) @PathParam("objectId") String objectId,
			@ApiParam(value = "Lower bound", required = true) @DefaultValue("0") @QueryParam("from") Long from,
			@ApiParam(value = "Upper bound", required = true) @DefaultValue("9223372036854775807") @QueryParam("to") Long to) { // def
		// Long.MAX_VALUE

		if (!revisionApi.verifyObject(serviceId, objectId)) {
			throw new ComotIllegalArgumentException("For service " + serviceId + " there is no managed object "
					+ objectId);
		}

		Change change = revisionApi.getAllChangesThatModifiedThisObject(serviceId, objectId, from, to);

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
