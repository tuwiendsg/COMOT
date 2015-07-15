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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import at.ac.tuwien.dsg.comot.m.common.InfoClient;
import at.ac.tuwien.dsg.comot.m.common.InfoServiceUtils;
import at.ac.tuwien.dsg.comot.m.common.exception.ComotException;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.Template;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

// WADL http://localhost:8380/comot/rest/application.wadl
@Service
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Path("/templates")
@Api(value = "/templates", description = "Templates for cloud services")
public class TemplatesResource {

	private static final Logger LOG = LoggerFactory.getLogger(TemplatesResource.class);

	@Autowired
	protected ToscaMapper mapperTosca;
	@Autowired
	protected InfoClient infoServ;

	@javax.annotation.Resource
	public Environment env;

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(
			value = "Create template",
			response = String.class)
	public Response createTemplate(
			@ApiParam(value = "Template of a cloud service", required = true) CloudService service)
			throws ComotException, JAXBException, ClassNotFoundException, IOException {

		infoServ.createTemplate(service);

		return Response.ok(service.getId()).build();
	}

	@POST
	@Path("/tosca")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(
			value = "Create template from TOSCA",
			response = String.class)
	public Response createTemplate(
			@ApiParam(value = "Template of a cloud service as TOSCA", required = true) Definitions def)
			throws ComotException, JAXBException, ClassNotFoundException, IOException {

		String templateId = infoServ.createTemplate(mapperTosca.createModel(def));

		return Response.ok(templateId).build();
	}

	@DELETE
	@Path("/{templateId}")
	@ApiOperation(
			value = "Delete template")
	public Response deleteTemplate(
			@ApiParam(value = "ID of the template", required = true) @PathParam("templateId") String templateId)
			throws ComotException {

		infoServ.removeTemplate(templateId);

		return Response.ok().build();
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@ApiOperation(
			value = "Get template",
			response = Template.class,
			responseContainer = "List")
	public Response getTemplates(
			@ApiParam(value = "Templates for certain type of cloud service to filter", required = false, allowableValues = InfoClient.ALL
					+ ", " + InfoClient.NON_EPS + ", " + InfoClient.EPS) @DefaultValue(InfoClient.NON_EPS) @QueryParam("type") String type)
			throws ClassNotFoundException, IOException, EpsException {

		type = type.toUpperCase();

		List<Template> all = infoServ.getTemplates();

		Set<String> dynamicEpsServices = new HashSet<String>();
		for (OfferedServiceUnit osu : infoServ.getOsus()) {
			if (InfoServiceUtils.isDynamicEps(osu)) {
				dynamicEpsServices.add(osu.getServiceTemplate().getId());
			}
		}

		if (InfoClient.ALL.equals(type)) {

		} else if (InfoClient.EPS.equals(type)) {

			for (Iterator<Template> iterator = all.iterator(); iterator.hasNext();) {
				Template one = iterator.next();
				if (!dynamicEpsServices.contains(one.getId())) {
					iterator.remove();
				}
			}

		} else if (InfoClient.NON_EPS.equals(type)) {

			for (Iterator<Template> iterator = all.iterator(); iterator.hasNext();) {
				Template one = iterator.next();
				if (dynamicEpsServices.contains(one.getId())) {
					iterator.remove();
				}
			}

		} else {
			all = new ArrayList<Template>();
		}

		return Response.ok(all.toArray(new Template[all.size()])).build();
	}

	@GET
	@Consumes(MediaType.WILDCARD)
	@Path("/{templateId}/tosca")
	@ApiOperation(
			value = "Get template definition as TOSCA",
			response = Definitions.class)
	public Response getTemplateTosca(
			@ApiParam(value = "ID of the template", required = true) @PathParam("templateId") String templateId)
			throws ClassNotFoundException, IOException, JAXBException, EpsException {

		CloudService service = infoServ.getTemplate(templateId).getDescription();

		return Response.ok(mapperTosca.extractTosca(service)).build();
	}

}
