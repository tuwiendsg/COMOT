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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import at.ac.tuwien.dsg.comot.m.ui.model.ServiceInstanceUi;
import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceEntity;
import at.ac.tuwien.dsg.comot.model.provider.OsuInstance;
import at.ac.tuwien.dsg.comot.model.type.DirectiveType;
import at.ac.tuwien.dsg.comot.model.type.State;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

// WADL http://localhost:8380/comot/rest/application.wadl
@Service
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Path("/services")
@Api(value = "/services", description = "Managed cloud services")
public class ServicesResource {

	private static final Logger LOG = LoggerFactory.getLogger(ServicesResource.class);

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

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(
			value = "Create service",
			response = String.class)
	public Response createService(
			@ApiParam(value = "Cloud service described in COMOT model", required = true) CloudService service)
			throws Exception {

		String serviceId = coordinator.createService(service);
		return Response.ok(serviceId).build();
	}

	@POST
	@Path("/tosca")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(
			value = "Create service from TOSCA",
			response = String.class)
	public Response createServiceFromTosca(
			@ApiParam(value = "Cloud service described as TOSCA", required = true) Definitions def) throws Exception {

		String serviceId = coordinator.createService(mapperTosca.createModel(def));
		return Response.ok(serviceId).build();
	}

	@POST
	@Path("/template")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(
			value = "Create service from template",
			response = String.class)
	public Response createServiceFromTemplate(
			@ApiParam(value = "ID of the source template", required = true) String templateId)
			throws Exception {

		String serviceId = coordinator.createServiceFromTemplate(templateId);
		return Response.ok(serviceId).build();
	}

	@DELETE
	@Path("/{serviceId}")
	@ApiOperation(
			value = "Delete cloud service",
			notes = "Succedes only if the service is in the state PASSIVE")
	public Response deleteService(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String serviceId)
			throws Exception {
		// TODO
		coordinator.removeService(serviceId);
		return Response.ok().build();
	}

	@PUT
	@Path("/{serviceId}/active")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(
			value = "Start cloud service")
	public Response startServiceInstance(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String serviceId)
			throws Exception {

		coordinator.startService(serviceId);
		return Response.ok().build();
	}

	@DELETE
	@Path("/{serviceId}/active")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(
			value = "Stop cloud service")
	public Response stopServiceInstance(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String serviceId)
			throws Exception {

		coordinator.stopService(serviceId);
		return Response.ok().build();
	}

	@PUT
	@Path("/{serviceId}/terminate")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(
			value = "Delete cloud service immediately",
			notes = "Issues all EPSs to terminate the support immediately and removes the service from the management system")
	public Response kill(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String serviceId)
			throws Exception {

		coordinator.kill(serviceId);
		return Response.ok().build();
	}

	@PUT
	@Path("/{serviceId}/eps/{epsId}")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(
			value = "Assign the EPS to support the cloud service")
	public Response assignSupportingEps(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String serviceId,
			@ApiParam(value = "ID of the EPS", required = true) @PathParam("epsId") String epsId)
			throws ComotException, ClassNotFoundException, IOException, JAXBException {

		coordinator.assignSupportingOsu(serviceId, epsId);
		return Response.ok().build();
	}

	@DELETE
	@Path("/{serviceId}/eps/{epsId}")
	@ApiOperation(
			value = "Terminate the support")
	public Response removeSupportingEps(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String serviceId,
			@ApiParam(value = "ID of the EPS", required = true) @PathParam("epsId") String epsId)
			throws ComotException, ClassNotFoundException, IOException, JAXBException {

		coordinator.removeAssignmentOfSupportingOsu(serviceId, epsId);
		return Response.ok().build();
	}

	@PUT
	@Path("/{serviceId}/eps/{epsId}/events/{eventName}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(
			value = "Produce custom event")
	public Response triggerCustomEvent(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String serviceId,
			@ApiParam(value = "ID of the EPS", required = true) @PathParam("epsId") String epsId,
			@ApiParam(value = "Name of the custom event", required = true) @PathParam("eventName") String eventName,
			@ApiParam(value = "Optional message", required = false) String optionalInput)
			throws ComotException, ClassNotFoundException, IOException, JAXBException {

		coordinator.triggerCustomEvent(serviceId, epsId, eventName, optionalInput);
		return Response.ok().build();
	}

	// update structure / requirements for monitoring / SYBL directives
	@PUT
	@Path("/{serviceId}")
	@ApiOperation(
			value = "Update cloud service")
	public Response updateService(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String serviceId,
			@ApiParam(value = "TOSCA", required = true) Definitions def) {

		return null;
	}

	@PUT
	@Path("/{serviceId}/elasticity")
	@ApiOperation(
			value = "Reconfigure elasticity",
			notes = "Extracts only SYBL directives from the service description and uses them to update the elastic behavior")
	public Response reconfigureElasticity(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String serviceId,
			@ApiParam(value = "Cloud service description", required = true) CloudService service)
			throws Exception {

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

		coordinator.reconfigureElasticity(serviceId, service);

		return Response.ok().build();
	}

	@GET
	@Path("/{serviceId}/events")
	@Consumes(SseFeature.SERVER_SENT_EVENTS)
	@Produces(SseFeature.SERVER_SENT_EVENTS)
	@ApiOperation(
			value = "Subscribe to events",
			notes = "Using Server Sent Events (SSE) subscribe to all events associated to the specified cloud service",
			response = EventOutput.class)
	public EventOutput getServerSentEvents(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String serviceId)
			throws Exception {

		final EventOutput eventOutput = new EventOutput();

		UiAdapter processor = context.getBean(UiAdapter.class);
		processor.setUiAdapter(serviceId, eventOutput);
		processor.checkClient();

		Manager manager = context.getBean(SingleQueueManager.class);
		manager.start("UI_" + UUID.randomUUID().toString(), processor);

		return eventOutput;
	}

	// READ

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/{serviceId}")
	@ApiOperation(
			value = "Get cloud service",
			notes = "Includes the entire information about cloud service from the information model as well as the last transition for each element of the service",
			response = ServiceInstanceUi.class)
	public Response getService(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String serviceId)
			throws ClassNotFoundException, IOException, EpsException {

		ServiceInstanceUi instanceUi = new ServiceInstanceUi(
				infoServ.getService(serviceId),
				lcManager.getCurrentState(serviceId));

		return Response.ok(instanceUi).build();
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/{serviceId}/tosca")
	@ApiOperation(
			value = "Get cloud service description in TOSCA",
			response = Definitions.class)
	public Response getServiceTosca(
			@ApiParam(value = "ID of the cloud service", required = true) @PathParam("serviceId") String serviceId)
			throws ClassNotFoundException, IOException, JAXBException, EpsException {

		CloudService service = infoServ.getService(serviceId);

		return Response.ok(mapperTosca.extractTosca(service)).build();
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@ApiOperation(
			value = "Get cloud services",
			response = CloudService.class,
			responseContainer = "List")
	public Response getServices(
			@ApiParam(value = "Type of services to filter", required = false, allowableValues = InformationClient.ALL
					+ ", " + InformationClient.NON_EPS + ", " + InformationClient.EPS) @DefaultValue(InformationClient.NON_EPS) @QueryParam("type") String type)
			throws ClassNotFoundException, IOException, EpsException {

		type = type.toUpperCase();

		List<CloudService> allServices = infoServ.getServices();

		Set<String> dynamicEpsServices = new HashSet<String>();
		for (OsuInstance osuInstance : infoServ.getOsuInstances()) {
			if (InformationClient.isDynamicEps(osuInstance.getOsu())) {
				dynamicEpsServices.add(osuInstance.getService().getId());
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

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/lifecycle/{level}")
	@ApiOperation(
			value = "Get lifecycle",
			response = LifeCycle.class)
	public Response getLifeCycle(
			@ApiParam(value = "Level for which to return the lifecycle", required = true, allowableValues = "SERVICE, TOPOLOGY, UNIT, INSTANCE") @DefaultValue("SERVICE") @PathParam("level") Type level) {

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

}
